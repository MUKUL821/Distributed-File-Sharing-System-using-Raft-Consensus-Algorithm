package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.paralleltasks.DBRequestFollowersThread;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasWorkSharingAckState extends WorkMessageState{
	public HasWorkSharingAckState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		logger.info("received worksharing req acknowledgementt with reqId"+msg.getWorkSharingRequestAck().getRequestId()+" success value: "+msg.getWorkSharingRequestAck().getSuccess());
		String requestId = msg.getWorkSharingRequestAck().getRequestId();
		boolean success =msg.getWorkSharingRequestAck().getSuccess();
		DBRequestFollowersThread.fileRequestClientMap.get(requestId).setReceivedAck(true);
		DBRequestFollowersThread.fileRequestClientMap.get(requestId).setResult(success);	
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
