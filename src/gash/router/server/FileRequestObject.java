package gash.router.server;

import gash.router.server.edges.EdgeInfo;
import io.netty.channel.ChannelHandlerContext;

public class FileRequestObject {
	private String requestId;
	private ChannelHandlerContext ctx;
	private String fileName;	
	private EdgeInfo ei;
	//either work or command or global
	private int desitinationId=-1;
	public int getDesitinationId() {
		return desitinationId;
	}
	public void setDesitinationId(int desitinationId) {
		this.desitinationId = desitinationId;
	}
	private String messageType;
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public EdgeInfo getEi() {
		return ei;
	}
	public void setEi(EdgeInfo ei) {
		this.ei = ei;
	}
	public FileRequestObject(ChannelHandlerContext ctx, String fileName, String messageType) {
		super();
		this.ctx = ctx;
		this.fileName = fileName;
		this.messageType=messageType;
	}
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
