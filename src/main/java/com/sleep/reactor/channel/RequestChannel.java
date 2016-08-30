package com.sleep.reactor.channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author yafeng.huang
 *
 */
public class RequestChannel<T> {

	/**
	 * processor 传递 request 给 handler 线程
	 */
	private BlockingQueue<T> reqQueue;

	/**
	 * handler 将处理完成的结果返回给 processor, 由 processor 负责返回给客户端
	 */
	private BlockingQueue<T>[] resQueues;

	public RequestChannel(int processorNum, int queueSize) {
		reqQueue = new ArrayBlockingQueue<T>(queueSize);
		resQueues = new ArrayBlockingQueue[processorNum];
		for (int i = 0; i < processorNum; i++) {
			resQueues[i] = new ArrayBlockingQueue<T>(queueSize);
		}
	}

	/**
	 * processor 将客户端请求发给 handler
	 * 
	 * @param t
	 * @throws InterruptedException
	 */
	public void putRequest(T t) throws InterruptedException {
		reqQueue.put(t);
	}
	
	public T pollRequest(long timeout) throws InterruptedException {
		return reqQueue.poll(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * handler 将处理结果发给 channel
	 * 
	 * @param processorId
	 * @param t
	 * @throws InterruptedException
	 */
	public void putResponse(int processorId, T t) throws InterruptedException {
		resQueues[processorId].put(t);
	}

	/**
	 * @param processorId
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	public T pollResponse(int processorId, long timeout) throws InterruptedException {
		return resQueues[processorId].poll(timeout, TimeUnit.MILLISECONDS);
	}

}
