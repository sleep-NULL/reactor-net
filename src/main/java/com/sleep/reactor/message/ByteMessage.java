package com.sleep.reactor.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

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

	private ByteBuffer payload;

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
	public byte[] toByteArray() {
		if (!isNull()) {
			ByteBuffer buf = ByteBuffer.allocate(SIZE_LENGTH + payload.limit());
			buf.putInt(length);
			buf.put(payload);
			buf.rewind();
			payload.rewind();
			return buf.array();
		}
		return null;
	}

	@Override
	public void read(ReadableByteChannel readableByteChannel) throws IOException {
		ByteBuffer size = ByteBuffer.allocate(SIZE_LENGTH);
		readableByteChannel.read(size);
		if (!size.hasRemaining()) {
			size.flip();
			this.length = size.getInt();
			if (length < 0 || length > MAX_SIZE) {
				throw new InvalidateMessageException("Invalidate message length, length is " + length);
			}
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
	public String toString() {
		return "ByteMessage [length=" + length + ", payload=" + payload + "]";
	}

}
