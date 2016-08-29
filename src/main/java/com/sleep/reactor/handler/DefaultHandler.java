package com.sleep.reactor.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleep.reactor.channel.DataChannel;
import com.sleep.reactor.message.ByteMessage;

/**
 * @author yafeng.huang
 *
 */
public class DefaultHandler implements Handler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultHandler.class);
	
	private DataChannel<ByteMessage> channel;
	
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public DefaultHandler(DataChannel<ByteMessage> channel) {
		this.channel = channel;
	}

	@Override
	public void handle() {
		while (isRunning.get()) {
			try {
				ByteMessage message = channel.poll(1000L);
				if (message != null) {
					System.out.println(message);
				}
			} catch (Exception e) {
				logger.error("Handle message error.");
			}
		}
	}

}
