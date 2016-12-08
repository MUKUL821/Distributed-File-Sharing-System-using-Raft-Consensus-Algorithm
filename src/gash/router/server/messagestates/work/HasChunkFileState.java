package gash.router.server.messagestates.work;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.FileReadRequestObject;
import gash.router.server.MessageServer;
import gash.router.server.WorkHandler;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.paralleltasks.DBRequestFollowersThread;
import gash.router.server.utils.GlobalMessageUtils;
import gash.router.server.utils.WorkMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasChunkFileState extends WorkMessageState{
	public HasChunkFileState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try
		{
			logger.info("received chunkfile from "+msg.getHeader().getNodeId());
			if(msg.getChunkFile().hasRequestId()){
				//logger.info("inside has request id");
				FileReadRequestObject frro = DBRequestFollowersThread.fileRequestClientMap.get(msg.getChunkFile().getRequestId());
				//if(frro.getCtx()!=null){
					routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
					routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
					routing.Pipe.Response.Builder res= routing.Pipe.Response.newBuilder();
					hb.setClusterId(999);
				 	hb.setTime(System.currentTimeMillis());
				    
					
					routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
				    file.setFilename(msg.getChunkFile().getFileName());
				    file.setData(msg.getChunkFile().getData());
				    file.setChunkId(msg.getChunkFile().getChunkId());
				    file.setChunkCount(msg.getChunkFile().getChunkCount());
				  
				    res.setFile(file.build());
				    res.setRequestId(msg.getChunkFile().getRequestId());
				    res.setRequestType(routing.Pipe.RequestType.READ);
					rb.setGlobalHeader(hb.build());
					rb.setResponse(res.build());
				//	ctx.channel().writeAndFlush(rb.build());
					frro.getCtx().channel().writeAndFlush(rb.build());
					/*
				} else {
					//Transfer global file to all nodes
					GlobalMessage gm = GlobalMessageUtils.createResponseWithFile(frro,msg);
					GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
				}*/
				
			} else {
				MongoDBHandler mongo = new MongoDBHandler();
				GuavaHandler guava = new GuavaHandler()  ;
				MessageServer.StartWorkCommunication.getInstance().upload(WorkMessageUtils.toCommandMsg(msg), mongo, guava,ctx);
			}
		}
		catch(Exception e)
		{
			logger.error("Exception occured will receiving chunk files : " + e.getMessage());
		}
	}

}
