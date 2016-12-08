package gash.router.server.paralleltasks;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.MessageServer;

import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.utils.ElectionUtil;
import gash.router.server.utils.GlobalMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;
import pipe.common.Common.Header;
import pipe.work.Work.ChunkFile;

import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;
import routing.Pipe.File;

public class DBReadRequestThread implements Runnable{
	
	private static Logger logger = LoggerFactory.getLogger("DBReadRequestThread");
	ChannelHandlerContext ctx;
	String fileName;
	MongoDBHandler dbhandler;
	String messageType;
	String requestId;
	int destinationId;
	 public DBReadRequestThread(ChannelHandlerContext ctx , String f, String messageType, String req, MongoDBHandler h, int destinationId) {
		// TODO Auto-generated constructor stub
		 fileName = f;
		 this.ctx = ctx;
		 this.dbhandler = h;
		 this.messageType=messageType;
		 this.requestId=req;
		 this.destinationId=destinationId;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			ArrayList<File> fileChunks = null;
			fileChunks =MessageServer.StartWorkCommunication.getInstance().read(fileName);
			
			System.out.println("Total filechunks retrieved"+fileChunks.size());
			for(File chunk : fileChunks) {
					if(messageType.equals("command")) {
						routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
						routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
						routing.Pipe.Response.Builder res= routing.Pipe.Response.newBuilder();
						hb.setClusterId(999);
					 	hb.setTime(System.currentTimeMillis());
					    
						
						routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
					    file.setFilename(chunk.getFilename());
					    file.setData(chunk.getData());
					    file.setChunkId(chunk.getChunkId());
					    file.setChunkCount(chunk.getChunkCount());
					  
					    res.setFile(file.build());
					    res.setRequestId("id");
					    res.setRequestType(routing.Pipe.RequestType.READ);
						rb.setGlobalHeader(hb.build());
						rb.setResponse(res.build());
						ctx.channel().writeAndFlush(rb.build());
					} else if(messageType.equals("work")) {
						WorkState.Builder sb = WorkState.newBuilder();
						sb.setEnqueued(-1);
						sb.setProcessed(-1);
						ChunkFile.Builder bb = ChunkFile.newBuilder();
					    bb.setChunkCount(chunk.getChunkCount());
					    bb.setChunkId(chunk.getChunkId());
					    bb.setData(chunk.getData());
					    bb.setFileName(chunk.getFilename());
					    bb.setRequestId(requestId);
						Header.Builder hb = Header.newBuilder();
						hb.setNodeId(ElectionUtil.state.getConf().getNodeId());
						hb.setDestination(2);
						hb.setTime(System.currentTimeMillis());

						WorkMessage.Builder wb = WorkMessage.newBuilder();
						wb.setHeader(hb);
						wb.setChunkFile(bb);
						wb.setSecret(100);
						ctx.channel().writeAndFlush(wb.build());
					} else if(messageType.equals("global")) {

						GlobalMessage gm = GlobalMessageUtils.createGlobalResponseMessage(destinationId,chunk.getFilename(),requestId,chunk.getChunkId(),chunk.getData(),chunk.getChunkCount());
						GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
					}
				}
			
			
		
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
	}
	
}
