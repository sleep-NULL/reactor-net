package com.sleep.reactor.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.message.ByteMessage;

public class Processor {

	private static final Logger logger = LoggerFactory.getLogger(Processor.class);

	private Selector selector;

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private BlockingQueue<SocketChannel> clientChannelQueue;

	public Processor() throws IOException {
		this.selector = Selector.open();
		this.clientChannelQueue = new ArrayBlockingQueue<SocketChannel>(100);
	}

	public void assign(SocketChannel socketChannel) throws InterruptedException {
		clientChannelQueue.put(socketChannel);
	}

	private void registry() {
		SocketChannel socketChannel = null;
		try {
			socketChannel = this.clientChannelQueue.poll(300, TimeUnit.MILLISECONDS);
			if (socketChannel != null) {
				socketChannel.configureBlocking(false);
				socketChannel.socket().setTcpNoDelay(true);
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
		} catch (Exception e) {
			logger.error("Client register OP_READ on Processor failed.", e);
			if (socketChannel != null) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					// ignore
				}
			}
		}
	}

	public void start() {
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
						if (key.isReadable()) {
							SocketChannel client = null;
							try {
								client = (SocketChannel) key.channel();
								getMessage(client);
							} catch (Exception e) {
								logger.error("Client occur error.", e);
								if (client != null) {
									try {
										client.close();
									} catch (Exception e1) {
										//ignore
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Processor occur error.", e);
			}
		}
	}

	private void getMessage(SocketChannel client) throws IOException {
		while (true) {
			ByteMessage message = new ByteMessage();
			message.read(client);
			if (message.getPayload() == null) {
				break;
			} else {
				System.out.println(new String(message.getPayload().array()));
			}
		}
	}

}
