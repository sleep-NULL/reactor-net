package com.sleep.reactor.channel;

/**
 * 顶级数据通道接口
 * 
 * @author yafeng.huang
 *
 */
public interface DataChannel<T> {
	
	/**
	 * 
	 * @param t
	 * @throws InterruptedException 
	 */
	public void put(T t) throws InterruptedException;
	
	/**
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	public T take() throws InterruptedException;
	
	/**
	 * 
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	public T poll(long timeout) throws InterruptedException;

}
