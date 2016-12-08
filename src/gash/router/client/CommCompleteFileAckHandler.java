package gash.router.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.work.Work.WorkMessage;

public class CommCompleteFileAckHandler extends SimpleChannelInboundHandler<WorkMessage> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		// TODO Auto-generated method stub
		if(msg.hasFileUploadedAck())
		{
			System.out.println("Complete File Transferred");
			// S
		}
	}
	
}
