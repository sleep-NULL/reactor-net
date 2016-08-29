package com.sleep.reactor.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.util.ThreadUtil;

/**
 * @author yafeng.huang
 *
 */
public class Acceptor {

	private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

	private Selector selector;

	private ServerSocketChannel serverSockerChannel;

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private Processor[] processors;

	private int roundRobinCount = 1;

	private int roundRobinNum;

	public Acceptor(String hostname, int port, int processorNum) throws IOException {
		this.selector = Selector.open();
		this.serverSockerChannel = ServerSocketChannel.open();
		this.serverSockerChannel.configureBlocking(false);
		this.serverSockerChannel.socket().bind(new InetSocketAddress(hostname, port));
		this.serverSockerChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.processors = new Processor[processorNum];
		this.roundRobinNum = processorNum;
	}

	public void start() throws IOException {
		isRunning.set(true);
		for (int i = 0; i < processors.length; i++) {
			final Processor processor = new Processor();
			processors[i] = processor;
			// 启动 processor 线程
			ThreadUtil.newThread(new Runnable() {
				@Override
				public void run() {
					processor.start();
				}
			}, "processor-" + i);
		}
		while (isRunning.get()) {
			try {
				int selectNum = selector.select(300L);
				if (selectNum != 0) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						if (key.isAcceptable()) {
							SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
							System.out.println(client.getRemoteAddress());
							dealClient(client);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Occur error accept client connection.", e);
			}
		}
	}

	private void dealClient(SocketChannel socketChannel) throws InterruptedException {
		int index = roundRobinCount % roundRobinNum;
		roundRobinCount = index + 1;
		processors[index].assign(socketChannel);
	}

}
