package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.utils.WorkStealingUtil;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasWorkStealingRequestState extends WorkMessageState {
	public HasWorkStealingRequestState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		logger.info("Received Work stealing request from Node"+msg.getHeader().getNodeId());
		WorkStealingUtil.workStealingActionHandler(msg.getHeader().getNodeId());
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
