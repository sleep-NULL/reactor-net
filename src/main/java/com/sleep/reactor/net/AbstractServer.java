package com.sleep.reactor.net;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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

	protected void closeChannel(SelectionKey key) {
		if (key != null) {
			SocketChannel channel = (SocketChannel) key.channel();
			closeChannel(channel);
		}
	}

}
