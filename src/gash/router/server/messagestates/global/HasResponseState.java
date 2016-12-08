package gash.router.server.messagestates.global;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.FileReadRequestObject;
import gash.router.server.paralleltasks.DBRequestFollowersThread;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;

public class HasResponseState extends GlobalMessageState {
public HasResponseState(GlobalMessage msg , ChannelHandlerContext ctx) {
	// TODO Auto-generated constructor stub
	this.msg = msg;
	this.ctx = ctx;
}
	@Override
	public void execute() {System.out.println("inside response hancdler");
		// TODO Auto-generated method stub
		if(msg.getGlobalHeader().getDestinationId()==GlobalEdgeMonitor.state.getgConf().getClusterId()){
			FileReadRequestObject frro = DBRequestFollowersThread.fileRequestClientMap.get(msg.getResponse().getRequestId());
			//if(frro.getCtx()!=null){
				routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
				routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
				routing.Pipe.Response.Builder res= routing.Pipe.Response.newBuilder();
				hb.setClusterId(999);
			 	hb.setTime(System.currentTimeMillis());
			    
				
				routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
			    file.setFilename(msg.getResponse().getFile().getFilename());
			    file.setData(msg.getResponse().getFile().getData());
			    file.setChunkId(msg.getResponse().getFile().getChunkId());
			    file.setChunkCount(msg.getResponse().getFile().getTotalNoOfChunks());
			  
			    res.setFile(file.build());
			    res.setRequestId(msg.getResponse().getRequestId());
			    res.setRequestType(routing.Pipe.RequestType.READ);
				rb.setGlobalHeader(hb.build());
				rb.setResponse(res.build());
			//	ctx.channel().writeAndFlush(rb.build());
				frro.getCtx().channel().writeAndFlush(rb.build());
		} else {
			GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
		}
	}

}
