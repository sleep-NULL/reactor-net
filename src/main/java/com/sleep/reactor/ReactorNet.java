package com.sleep.reactor;

import java.io.IOException;

import com.sleep.reactor.net.Acceptor;

public class ReactorNet {
	
	public static void main(String[] args) throws IOException {
		Acceptor acceptor = new Acceptor("localhost", 4314, 5);
		acceptor.start();
	}

}
