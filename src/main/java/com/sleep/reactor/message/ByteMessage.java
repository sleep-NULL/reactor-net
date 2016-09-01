package com.sleep.reactor.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author yafeng.huang
 *
 */
public class ByteMessage implements Message {

	private static final int SIZE_LENGTH = 4;
	
	private static final int MAX_SIZE = Integer.MAX_VALUE;

	/**
	 * 消息长度
	 */
	private int length;
	
	private ByteBuffer size = ByteBuffer.allocate(SIZE_LENGTH);

	private ByteBuffer payload;
	
	private ByteBuffer writeBuffer;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public ByteBuffer getPayload() {
		return payload;
	}

	public void setPayload(ByteBuffer payload) {
		this.payload = payload;
	}


	@Override
	public void read(ReadableByteChannel readableByteChannel) throws IOException {
		readableByteChannel.read(size);
		if (!size.hasRemaining()) {
			size.flip();
			this.length = size.getInt();
			if (length < 0 || length > MAX_SIZE) {
				throw new InvalidateMessageException("Invalidate message length, length is " + length);
			}
			size.rewind();
			payload = ByteBuffer.allocate(length);
			readableByteChannel.read(payload);
			if (!payload.hasRemaining()) {
				payload.flip();
			} else {
				payload = null;
			}
		}
	}
	
	public boolean isNull() {
		return payload == null;
	}

	@Override
	public void write(SocketChannel channel) throws IOException {
		if (writeBuffer == null) {
			writeBuffer = ByteBuffer.allocate(SIZE_LENGTH + this.length);
			writeBuffer.putInt(this.length);
			writeBuffer.put(payload.array());
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
		channel.write(writeBuffer);
	}

	@Override
	public boolean complete() {
		return writeBuffer != null && !writeBuffer.hasRemaining();
	}
	
	public ByteMessage build(byte[] buf) {
		if (buf == null) {
			throw new IllegalArgumentException("Args can't be null.");
		}
		ByteMessage message = new ByteMessage();
		message.setLength(buf.length);
		message.size.putInt(buf.length);
		message.size.flip();
		message.setPayload(ByteBuffer.wrap(buf));
		return message;
	}

}
