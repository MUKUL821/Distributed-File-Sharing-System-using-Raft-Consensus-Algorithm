package gash.router.server.messagestates.work;

import gash.router.server.WorkHandler;
import gash.router.server.edges.LeaderDetails;
import gash.router.server.utils.ElectionUtil;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasVoteResponseState extends WorkMessageState {
	public HasVoteResponseState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			logger.info("received vote response with term"+msg.getVoteResponse().getTerm());
			if(msg.getVoteResponse().getTerm()==LeaderDetails.getTerm()){
				ElectionUtil.notRespondedEdges.remove(msg.getHeader().getNodeId());
				ElectionUtil.totalVoteReceivedCount+=1;
				//remove the below if
				if(!ElectionUtil.notRespondedEdges.containsKey(msg.getHeader().getNodeId())){
					if(msg.getVoteResponse().getIsVoted()){
						ElectionUtil.voteYes+=1;
					}
					else{
						ElectionUtil.voteNo+=1;
					}			
				}
			}
			else {
				//logger.info("Discarding stale vote");
			}
		}
		catch(Exception e){
			logger.error("Caught exception");
		}
		
	}

}
