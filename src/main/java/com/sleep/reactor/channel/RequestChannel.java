package com.sleep.reactor.channel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sleep.reactor.message.Message;

/**
 * @author yafeng.huang
 *
 */
public class RequestChannel implements DataChannel<Message> {

	private BlockingQueue<Message> queue;
	
	@Override
	public void put(Message t) throws InterruptedException {
		queue.put(t);
	}

	@Override
	public Message take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public Message poll(long timeout) throws InterruptedException {
		return queue.poll(timeout, TimeUnit.MILLISECONDS);
	}

}
