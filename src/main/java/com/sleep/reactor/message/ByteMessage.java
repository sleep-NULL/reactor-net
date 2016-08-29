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

	/**
	 * 消息长度
	 */
	private int length;
	
	private static final int SIZE_LENGTH = 4;
	
	private static final int MAX_SIZE = Integer.MAX_VALUE;

	/**
	 * 存储消息长度的 buffer,int 需要 4 个字节存储
	 */
	private ByteBuffer size = ByteBuffer.allocate(SIZE_LENGTH);

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
		if (payload != null) {
			ByteBuffer buf = ByteBuffer.allocate(SIZE_LENGTH + payload.position());
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
		readableByteChannel.read(size);
		if (!size.hasRemaining()) {
			size.flip();
			this.length = size.getInt();
			if (length < 0 || length > MAX_SIZE) {
				throw new InvalidateMessageException("Invalidate message length, length is " + length);
			}
			payload = ByteBuffer.allocate(length);
			readableByteChannel.read(payload);
			payload.flip();
		}
	}

	@Override
	public String toString() {
		return "ByteMessage [length=" + length + ", payload=" + payload + "]";
	}
	
}
