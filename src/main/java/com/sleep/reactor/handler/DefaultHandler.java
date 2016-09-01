package com.sleep.reactor.handler;

import com.sleep.reactor.message.ByteMessage;
import com.sleep.reactor.net.ReqOrRes;

/**
 * @author yafeng.huang
 *
 */
public class DefaultHandler implements Handler<ReqOrRes, ReqOrRes> {

	@Override
	public ReqOrRes handle(ReqOrRes reqOrRes) {
		ByteMessage message = reqOrRes.getMessage();
		System.out.println(Thread.currentThread().getName() + " === " + new String(message.getPayload().array()));
		return ReqOrRes.buildReqOrRes(reqOrRes.getClientId(), reqOrRes.getProcessorId(), message);
	}

}
