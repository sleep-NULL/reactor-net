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
		client.socket().bind(new InetSocketAddress("localhost", 3000));
		client.configureBlocking(false);
		client.connect(new InetSocketAddress("localhost", 4314));
		client.register(selector, SelectionKey.OP_CONNECT);
		while (true) {
			int num = selector.select(1000L);
			if (num > 0) {
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isConnectable()) {
						if (client.isConnectionPending()) {
							if (client.finishConnect()) {
								System.out.println("connect");
								// key.channel().register(selector,
								// SelectionKey.OP_WRITE);
								System.out.println("write");
								for (int i = 0; i < Integer.MAX_VALUE; i++) {
									ByteBuffer buf = ByteBuffer.allocate(1024);
									buf.putInt(("hello world" + i).length());
									buf.put(("hello world" + i).getBytes());
									buf.flip();
									((SocketChannel) key.channel()).write(buf);
									try {
										Thread.sleep(10L);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
