package gash.router.server.messagestates.global;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;

public class HasMessageState extends GlobalMessageState{
public HasMessageState(GlobalMessage msg , ChannelHandlerContext ctx) {
	// TODO Auto-generated constructor stub
		this.msg = msg;
		this.ctx = ctx;
	}
	@Override
	public void execute() {
		/*
		GlobalLeaderDetails.receivedHBFromLeader=true;
		SocketAddress remoteAddress = ctx.channel().remoteAddress();
		InetSocketAddress addr = (InetSocketAddress) remoteAddress;
		GlobalEdgeInfo ei= new GlobalEdgeInfo(msg.getGlobalHeader().getClusterId(), addr.toString(), msg.getLetsConnect().getPort());
		GlobalLeaderDetails.ei=ei;*/
	}	

}
