package gash.router.server;

import io.netty.channel.ChannelHandlerContext;

public class FileReadRequestObject {	
	private String fileName;
	private int nodeId;
	private boolean result;
	private boolean receivedAck = false;
	private ChannelHandlerContext ctx;
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	public boolean isReceivedAck() {
		return receivedAck;
	}
	public void setReceivedAck(boolean receivedAck) {
		this.receivedAck = receivedAck;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}

}
