package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasPingState extends WorkMessageState{
	public HasPingState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		WorkHandler.logger.info("ping from " + msg.getHeader().getNodeId());
		//boolean p = msg.getPing();
		WorkMessage.Builder rb = WorkMessage.newBuilder();
		rb.setPing(true);
		ctx.channel().write(rb.build());	
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
