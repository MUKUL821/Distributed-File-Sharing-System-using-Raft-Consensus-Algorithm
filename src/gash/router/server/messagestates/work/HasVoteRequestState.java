package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.edges.EdgeList;
import gash.router.server.utils.ElectionUtil;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasVoteRequestState extends WorkMessageState{
	public HasVoteRequestState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		if(EdgeList.map.containsKey(msg.getHeader().getNodeId())){
			ElectionUtil.state=workHandler.getState();
			ElectionUtil.createVoteResponse(msg.getVoteRequest(),EdgeList.map.get(msg.getHeader().getNodeId()));
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}
