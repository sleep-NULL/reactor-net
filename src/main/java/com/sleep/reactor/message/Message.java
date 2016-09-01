package com.sleep.reactor.message;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

/**
 * @author yafeng.huang
 *
 */
public interface Message {

	public void read(ReadableByteChannel readableByteChannel) throws IOException;
	
	public void write(SocketChannel channel) throws IOException;
	
	public boolean complete();
	
}
