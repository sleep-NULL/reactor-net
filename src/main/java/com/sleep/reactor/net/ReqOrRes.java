package com.sleep.reactor.net;

import com.sleep.reactor.message.ByteMessage;

public class ReqOrRes {

	/**
	 * 客户端的主机端口
	 */
	private String clientId;

	private int processorId;

	private ByteMessage message;

	public ReqOrRes(String clientId, int processorId, ByteMessage message) {
		super();
		this.clientId = clientId;
		this.processorId = processorId;
		this.message = message;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getProcessorId() {
		return processorId;
	}

	public void setProcessorId(int processorId) {
		this.processorId = processorId;
	}

	public ByteMessage getMessage() {
		return message;
	}

	public void setMessage(ByteMessage message) {
		this.message = message;
	}

	public static ReqOrRes buildReqOrRes(String clientId, int processorId, ByteMessage message) {
		return new ReqOrRes(clientId, processorId, message);
	}

}
