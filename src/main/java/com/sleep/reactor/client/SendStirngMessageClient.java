package com.sleep.reactor.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SendStirngMessageClient {

	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
		client.connect(new InetSocketAddress("localhost", 4314));
		client.register(selector, SelectionKey.OP_CONNECT);
		while (true) {
			int num = selector.selectNow();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isConnectable()) {
					System.out.println("connect");
					((SocketChannel)key.channel()).register(selector, SelectionKey.OP_WRITE);
				} else if (key.isWritable()) {
					ByteBuffer buf = ByteBuffer.allocate(4 + "hello world".getBytes().length);
					buf.putInt("hello world".length());
					buf.put("hello world".getBytes());
					((SocketChannel) key.channel()).write(buf);
					System.out.println("write");
				}
			}
		}
	}

}
