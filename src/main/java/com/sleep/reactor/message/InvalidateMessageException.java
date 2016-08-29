package com.sleep.reactor.message;

/**
 * 
 * @author yafeng.huang
 *
 */
public class InvalidateMessageException extends RuntimeException {

	private static final long serialVersionUID = -5587496277146185649L;
	
	public InvalidateMessageException(String message) {
		super(message);
	}
	
    public InvalidateMessageException(String message, Throwable cause) {
        super(message, cause);
    }
	
}
