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

import com.sleep.reactor.channel.RequestChannel;
import com.sleep.reactor.util.ThreadUtil;

/**
 * @author yafeng.huang
 *
 */
public class Acceptor extends AbstractServer {

	private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

	private Selector selector;

	private ServerSocketChannel serverSockerChannel;

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private Processor[] processors;

	private int roundRobinCount = 1;

	private int roundRobinNum;
	
	private RequestChannel<ReqOrRes> requestChannel;

	public Acceptor(String hostname, int port, int processorNum, RequestChannel<ReqOrRes> requestChannel) throws IOException {
		this.requestChannel = requestChannel;
		this.selector = Selector.open();
		this.serverSockerChannel = ServerSocketChannel.open();
		this.serverSockerChannel.configureBlocking(false);
		this.serverSockerChannel.socket().bind(new InetSocketAddress(hostname, port));
		this.serverSockerChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.processors = new Processor[processorNum];
		this.roundRobinNum = processorNum;
	}

	public void start() throws IOException {
		if (isRunning.get()) {
			return;
		}
		isRunning.set(true);
		for (int i = 0; i < processors.length; i++) {
			final Processor processor = new Processor(i, requestChannel);
			processors[i] = processor;
			// 启动 processor 线程
			ThreadUtil.newThread(new Runnable() {
				@Override
				public void run() {
					processor.start();
				}
			}, "processor-" + i);
		}
		SelectionKey key = null;
		while (isRunning.get()) {
			try {
				int selectNum = selector.select(300L);
				if (selectNum != 0) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						key = it.next();
						it.remove();
						if (key.isAcceptable()) {
							accept(key);
						} else {
							closeChannel(key.channel());
							logger.error("Invalidate key in Acceptor");
						}
					}
				}
			} catch (Exception e) {
				closeChannel(key.channel());
				logger.error("Occur error accept client connection.", e);
			}
		}
	}

	private void accept(SelectionKey key) throws InterruptedException, IOException {
		SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
		int index = roundRobinCount % roundRobinNum;
		roundRobinCount = index + 1;
		processors[index].assign(client);
		System.out.println("assign to processor " + index);
	}

}
