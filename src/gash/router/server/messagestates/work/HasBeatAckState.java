package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.edges.EdgeList;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasBeatAckState extends WorkMessageState{
	public HasBeatAckState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try{
		EdgeList.edgeStatus.put(msg.getHeader().getNodeId(), true);
		EdgeList.activeEdges.put(msg.getHeader().getNodeId(), EdgeList.map.get(msg.getHeader().getNodeId()));
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
}
