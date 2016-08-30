package com.sleep.reactor.handler;

/**
 * @author yafeng.huang
 *
 */
public interface Handler<X,Y> {
	
	/**
	 * 处理具体的客户端请求
	 */
	public Y handle(X x);

}
