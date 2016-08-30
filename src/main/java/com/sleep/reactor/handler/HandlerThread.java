package com.sleep.reactor.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.channel.RequestChannel;
import com.sleep.reactor.net.ReqOrRes;

public class HandlerThread implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerThread.class);

	private RequestChannel<ReqOrRes> requestChannel;

	private Handler<ReqOrRes, ReqOrRes> handler;

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	public HandlerThread(RequestChannel<ReqOrRes> requestChannel, Handler<ReqOrRes, ReqOrRes> handler) {
		this.requestChannel = requestChannel;
		this.handler = handler;
	}

	@Override
	public void run() {
		isRunning.set(true);
		ReqOrRes req = null;
		while (isRunning.get()) {
			try {
				if ((req = requestChannel.pollRequest(300L)) != null) {
					ReqOrRes res = handler.handle(req);
					if (res != null) {
						requestChannel.putResponse(res.getProcessorId(), res);
					}
				}
			} catch (InterruptedException e) {
				logger.error("Handler request occur error.");
			}
		}
	}

}
