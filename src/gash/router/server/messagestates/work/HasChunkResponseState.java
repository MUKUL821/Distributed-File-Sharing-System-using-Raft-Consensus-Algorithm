package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasChunkResponseState extends WorkMessageState{
	public HasChunkResponseState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		logger.info("received msg inside work handler from node"+msg.getHeader().getNodeId()+" chunk id "+msg.getChunkResponse().getChunkId());
	}

}
