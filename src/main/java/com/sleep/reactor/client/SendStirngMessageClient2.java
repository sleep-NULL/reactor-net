package com.sleep.reactor.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendStirngMessageClient2 {

	public static void main(String[] args) throws IOException {
		Socket client = new Socket();
		client.connect(new InetSocketAddress("localhost", 4314));
		OutputStream out = client.getOutputStream();
		int i = 0;
		while (i < 1000) {
			out.write(0);
			out.write(0);
			out.write(0);
			out.write(("hello world" + i).getBytes().length);
			out.write(("hello world" + i).getBytes());
			i++;
		}
		try {
			Thread.sleep(1000L * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
