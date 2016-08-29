package com.sleep.reactor.message;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * @author yafeng.huang
 *
 */
public interface Message {

	public void read(ReadableByteChannel readableByteChannel) throws IOException;
	
	public byte[] toByteArray();
	
}
