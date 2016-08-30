package com.sleep.reactor.net;

public class NetworkException extends RuntimeException {

	private static final long serialVersionUID = -4623030968944957332L;

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
