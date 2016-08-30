package com.sleep.reactor.net;

import java.io.IOException;
import java.nio.channels.Channel;

/**
 * @author yafeng.huang
 *
 */
public abstract class AbstractServer {
	
	protected void closeChannel(Channel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

}
