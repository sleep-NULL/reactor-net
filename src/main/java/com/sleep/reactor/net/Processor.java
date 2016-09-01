package com.sleep.reactor.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
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

public class Processor extends AbstractServer implements Runnable {

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

	public Processor(int processorId, RequestChannel<ReqOrRes> requestChannel) {
		this.processorId = processorId;
		this.clients = new ConcurrentHashMap<String, SocketChannel>();
		this.requestChannel = requestChannel;
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			throw new NetworkException("Processor open selector failed.", e);
		}
		this.clientChannelQueue = new ArrayBlockingQueue<SocketChannel>(100);
	}

	public void assign(SocketChannel socketChannel) throws InterruptedException {
		clientChannelQueue.put(socketChannel);
	}

	private void registry() throws InterruptedException, IOException {
		SocketChannel socketChannel = null;
		socketChannel = this.clientChannelQueue.poll(300, TimeUnit.MILLISECONDS);
		if (socketChannel != null) {
			socketChannel.configureBlocking(false);
			socketChannel.socket().setTcpNoDelay(true);
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
			clients.put(getClientId(socketChannel), socketChannel);
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
	
	private void processResponse() throws ClosedChannelException, InterruptedException {
		ReqOrRes res = requestChannel.pollResponse(processorId, 300L);
		if (res != null) {
			System.out.println(res.getClientId());
			System.out.println(res.getProcessorId());
			SocketChannel client = clients.get(res.getClientId());
			client.register(selector, SelectionKey.OP_WRITE, res);
		}
	}

	private String getClientId(SocketChannel client) {
		Socket socket = client.socket();
		return socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
	}

	@Override
	public void run() {
		if (isRunning.get()) {
			return;
		}
		isRunning.set(true);
		while (isRunning.get()) {
			// 从 queue 中获取新的客户端连接进行 OP_READ 注册
			SocketChannel client = null;
			try {
				registry();
				processResponse();
				int selectNum = selector.select(300L);
				if (selectNum != 0) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						if (key.isReadable()) {
							client = (SocketChannel) key.channel();
							readMessage(client);
						} else if (key.isWritable()) {
							write(key);
						}
						// 非法的 key 进行通道的关闭操作
						if (!key.isValid()) {
							closeChannel(client);
						}
					}
				}
			} catch (Throwable e) {
				closeChannel(client);
				logger.error("Processor occur error.", e);
			}
		}
	}

	private void write(SelectionKey key) throws IOException {
		ReqOrRes res = (ReqOrRes)key.attachment();
		if (res.getMessage().complete()) {
			key.interestOps(SelectionKey.OP_READ);
		} else {
			res.getMessage().write((SocketChannel)key.channel());
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

}
