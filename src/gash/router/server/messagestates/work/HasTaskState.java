package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;

public class HasTaskState extends WorkMessageState{
	public HasTaskState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		Task t = msg.getTask();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
