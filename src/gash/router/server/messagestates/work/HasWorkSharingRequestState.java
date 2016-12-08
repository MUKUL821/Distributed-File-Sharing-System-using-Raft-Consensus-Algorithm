package gash.router.server.messagestates.work;

import gash.router.server.FileRequestContainer;
import gash.router.server.FileRequestObject;
import gash.router.server.MessageServer;
import gash.router.server.WorkHandler;
import io.netty.channel.ChannelHandlerContext;
import pipe.common.Common.Header;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkSharingRequestAck;
import pipe.work.Work.WorkState;

public class HasWorkSharingRequestState extends WorkMessageState{
	public HasWorkSharingRequestState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			logger.info("has work sharing request");
			String fileName = msg.getWorkSharingRequest().getFileName();
			WorkState.Builder sb = WorkState.newBuilder();
			sb.setEnqueued(-1);
			sb.setProcessed(-1);
	
			WorkSharingRequestAck.Builder bb = WorkSharingRequestAck.newBuilder();
			bb.setFileName(msg.getWorkSharingRequest().getFileName());
			bb.setRequestId(msg.getWorkSharingRequest().getRequestId());
	
			Header.Builder hb = Header.newBuilder();
			hb.setNodeId(workHandler.getState().getConf().getNodeId());
			hb.setDestination(2);
			hb.setTime(System.currentTimeMillis());
	
			if(MessageServer.StartWorkCommunication.getInstance().check(fileName)){
				//send WorkSharingResponse - yes
				bb.setSuccess(true);
				FileRequestObject fro = new FileRequestObject(ctx, fileName, "work");
				fro.setRequestId(msg.getWorkSharingRequest().getRequestId());
				// Adding all the read requests to the request queue
				//logger.info("before adding to queue"+fro.getRequestId());
				FileRequestContainer.readRequestQueue.add(fro);
				
			} else {
				//send worksharing response no
				bb.setSuccess(false);
				
			}
			WorkMessage.Builder wb = WorkMessage.newBuilder();
			wb.setHeader(hb);
			wb.setWorkSharingRequestAck(bb);
			wb.setSecret(100);
			ctx.channel().writeAndFlush(wb.build());
		}
		catch(Exception e)
		{
			logger.info("error when Work Sharing request received :" + e.getMessage());
		}
	}

}
