package gash.router.server.messagestates.global;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.ServerState;
import gash.router.server.utils.GlobalMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;

public class HasPingState extends GlobalMessageState{
	public HasPingState(GlobalMessage msg , ChannelHandlerContext ctx) {
		// TODO Auto-generated constructor stub
		this.msg = msg;
		this.ctx = ctx;
	}
	@Override
	public void execute() {
		System.out.println("Received ping");
		System.out.println(msg);
		// TODO Auto-generated method stub
		if(ServerState.state.equals("Leader")){
			if(msg.getGlobalHeader().getDestinationId()==GlobalEdgeMonitor.state.getgConf().getClusterId())
				System.out.println("got my ping message");
			else {
				System.out.println("Received Ping forwarding to next cluster");
				GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
			}
		}
		
		//ctx.channel().writeAndFlush(GlobalMessageUtils.createPing());
	}

}
