package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.utils.PrintUtil;
import io.netty.channel.ChannelHandlerContext;
import pipe.common.Common.Failure;
import pipe.work.Work.WorkMessage;

public class HasErrorState extends WorkMessageState{
	public HasErrorState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		Failure err = msg.getErr();
		WorkHandler.logger.error("failure from " + msg.getHeader().getNodeId());
		PrintUtil.printFailure(err);
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
