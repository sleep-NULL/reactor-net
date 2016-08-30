package com.sleep.reactor.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.channel.RequestChannel;
import com.sleep.reactor.message.ByteMessage;

public class Processor extends AbstractServer {

	private static final Logger logger = LoggerFactory.getLogger(Processor.class);
	
	private int processorId;

	private Selector selector;
	
	private Map<String, SocketChannel> clients;

	private AtomicBoolean isRunning = new AtomicBoolean(false);
	
	private RequestChannel<ReqOrRes> requestChannel;

	/**
	 * 用于同 acceptor 交互
	 */
	private BlockingQueue<SocketChannel> clientChannelQueue;
	
	public Processor(int processorId, RequestChannel<ReqOrRes> requestChannel) throws IOException {
		this.processorId = processorId;
		this.clients = new ConcurrentHashMap<String, SocketChannel>();
		this.requestChannel = requestChannel;
		this.selector = Selector.open();
		this.clientChannelQueue = new ArrayBlockingQueue<SocketChannel>(100);
	}

	public void assign(SocketChannel socketChannel) throws InterruptedException {
		clientChannelQueue.put(socketChannel);
		System.out.println(Thread.currentThread().getName() + " : " + clientChannelQueue.size());
	}

	private void registry() {
		SocketChannel socketChannel = null;
		try {
			socketChannel = this.clientChannelQueue.poll(300, TimeUnit.MILLISECONDS);
			if (socketChannel != null) {
				socketChannel.configureBlocking(false);
				socketChannel.socket().setTcpNoDelay(true);
				SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
			}
		} catch (Exception e) {
			closeChannel(socketChannel);
			logger.error("Client register OP_READ on Processor failed.", e);
		}
	}

	public void start() {
		if (isRunning.get()) {
			return;
		}
		isRunning.set(true);
		while (isRunning.get()) {
			// 从 queue 中获取新的客户端连接进行 OP_READ 注册
			registry();
			try {
				int selectNum = selector.select(300L);
				if (selectNum != 0) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						SocketChannel client = null;
						if (key.isReadable()) {
							try {
								client = (SocketChannel) key.channel();
								readMessage(client);
							} catch (Exception e) {
								closeChannel(client);
								logger.error("Client occur error.", e);
							}
						}
						// 非法的 key 进行通道的关闭操作
						if (!key.isValid()) {
							closeChannel(client);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Processor occur error.", e);
			}
		}
	}

	private void readMessage(SocketChannel client) throws IOException, InterruptedException {
		while (true) {
			ByteMessage message = new ByteMessage();
			message.read(client);
			if (!message.isNull()) {
				requestChannel.putRequest(ReqOrRes.buildReqOrRes(getClientId(client), processorId, message));
			} else {
				break;
			}
		}
	}
	
	private String getClientId(SocketChannel client) {
		Socket socket = client.socket();
		return socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
	}

}
