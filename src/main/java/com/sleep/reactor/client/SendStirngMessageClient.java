package com.sleep.reactor.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.sleep.reactor.message.ByteMessage;

public class SendStirngMessageClient {

	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
		client.connect(new InetSocketAddress("localhost", 4314));
		client.register(selector, SelectionKey.OP_CONNECT);
		while (true) {
			int num = selector.select(10L);
			if (num > 0) {
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isConnectable()) {
						if (client.isConnectionPending()) {
							if (client.finishConnect()) {
								System.out.println("connect");
								System.out.println("write");
								ByteBuffer buf = ByteBuffer.allocate(1024);
								buf.putInt(("wtf world").length());
								buf.put(("wtf world").getBytes());
								buf.flip();
								((SocketChannel) key.channel()).write(buf);
								key.interestOps(SelectionKey.OP_READ);
							}
						}
					} else if (key.isReadable()) {
						ByteMessage message = new ByteMessage();
						message.read(client);
						System.out.println(new String(message.getPayload().array(), "UTF-8"));
						key.interestOps(SelectionKey.OP_WRITE);
					} else if (key.isWritable()) {
						ByteBuffer buf = ByteBuffer.allocate(1024);
						buf.putInt(("wtf world").length());
						buf.put(("wtf world").getBytes());
						buf.flip();
						((SocketChannel) key.channel()).write(buf);
						key.interestOps(SelectionKey.OP_READ);
					}
				}
			}
		}
	}

}
