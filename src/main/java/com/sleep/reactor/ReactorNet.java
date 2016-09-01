package com.sleep.reactor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.channel.RequestChannel;
import com.sleep.reactor.handler.DefaultHandler;
import com.sleep.reactor.handler.HandlerThread;
import com.sleep.reactor.net.Acceptor;
import com.sleep.reactor.net.ReqOrRes;
import com.sleep.reactor.util.ThreadUtil;

public class ReactorNet {

	private static final Logger logger = LoggerFactory.getLogger(ReactorNet.class);

	public static void main(String[] args) throws IOException {
		int processorNum = 1;
		int handlerNum = 3;
		RequestChannel<ReqOrRes> requestChannel = new RequestChannel<ReqOrRes>(processorNum, 100);
		ThreadUtil.newThread(new Acceptor("localhost", 4314, processorNum, requestChannel), "Acceptor");
		logger.info("Acceptor start.");
		DefaultHandler handler = new DefaultHandler();
		for (int i = 0; i < handlerNum; i++) {
			ThreadUtil.newThread(new HandlerThread(requestChannel, handler), "Handler-" + i);
		}
		logger.info("Handler start.");
	}

}
