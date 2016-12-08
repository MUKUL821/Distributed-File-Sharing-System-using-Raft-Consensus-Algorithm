package gash.router.server.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import gash.router.global.edges.GlobalEdgeInfo;
import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.FileReadRequestObject;
import gash.router.server.GlobalHandler;
import gash.router.server.ServerState;
import gash.router.server.edges.LeaderDetails;
import gash.router.server.messagestates.global.HasMessageState;
import gash.router.server.messagestates.global.HasPingState;
import gash.router.server.messagestates.global.HasRequestState;
import gash.router.server.messagestates.global.HasResponseState;
import global.Global.File;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import global.Global.Request;
import global.Global.RequestType;
import global.Global.Response;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class GlobalMessageUtils {
	
	public static void setGlobalMessageState(GlobalMessage msg , ChannelHandlerContext ctx, GlobalHandler globalHandler)
	{
		if(msg.hasPing())
		{
			globalHandler.setMsgState(new HasPingState(msg, ctx));
		}
		else if(msg.hasRequest())
		{
			globalHandler.setMsgState(new HasRequestState(msg, ctx));
		}
		else if(msg.hasResponse())
		{
			globalHandler.setMsgState(new HasResponseState(msg, ctx));
		}		
		else if(msg.hasMessage())
		{
			globalHandler.setMsgState(new HasMessageState(msg, ctx));
		}
		
	}
	
	

	
	public static GlobalMessage createMessage(GlobalEdgeInfo ei) {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(ei.getRef());

		GlobalMessage.Builder gm = GlobalMessage.newBuilder();
		gm.setGlobalHeader(header);
		gm.setMessage("Heartbeat");
		return gm.build();
	}

	public static GlobalMessage createPing() {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		
		GlobalMessage.Builder gm = GlobalMessage.newBuilder();
		gm.setGlobalHeader(header);
		gm.setPing(true);
		return gm.build();
	}
	
	public static GlobalMessage createGlobalRequestMessage(String filename,String requestId) {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		
	    Request.Builder rb = Request.newBuilder();
	    rb.setRequestId(requestId);
	    rb.setRequestType(RequestType.READ);
	    rb.setFileName(filename);
	    
	    GlobalMessage.Builder gb = GlobalMessage.newBuilder();
	    gb.setGlobalHeader(header);
	    gb.setRequest(rb);    
	    return gb.build();
	}

	public static GlobalMessage createGlobalResponseMessage(int destinationId, String fileName, String requestId, int chunkId, ByteString data, int totalCount) {
		System.out.println(destinationId+","+fileName+","+requestId+","+chunkId+","+data+","+totalCount);
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(destinationId);
		System.out.println("after header");
		 System.out.println("requestId:"+requestId.length());
		    Response.Builder rb = Response.newBuilder();
		    rb.setRequestId(requestId);
		    rb.setRequestType(RequestType.READ);

			System.out.println("after response builder");
		    File.Builder fb = File.newBuilder();
		    fb.setChunkId(chunkId);
		    fb.setData(data);
		    fb.setFilename(fileName);
		    fb.setTotalNoOfChunks(totalCount);

			System.out.println("after file builder");
		    GlobalMessage.Builder gb = GlobalMessage.newBuilder();
		    gb.setGlobalHeader(header);
		    gb.setResponse(rb.setFile(fb)); 

			System.out.println("after global message builder");
			
		    return gb.build();
	}
	public static GlobalMessage createGlobalRequestMessageWrite(int destinationId, String fileName, String requestId, int chunkId, ByteString data, int totalCount) {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		
		File.Builder fb = File.newBuilder();
	    fb.setChunkId(chunkId);
	    fb.setData(data);
	    fb.setFilename(fileName);
	    fb.setTotalNoOfChunks(totalCount);
		
	    Request.Builder rb = Request.newBuilder();
	    rb.setRequestId(requestId);
	    rb.setRequestType(RequestType.WRITE);
	    
	    rb.setFile(fb);
	    
	    GlobalMessage.Builder gb = GlobalMessage.newBuilder();
	    gb.setGlobalHeader(header);
	    gb.setRequest(rb);    
	    return gb.build();
	}
	public static GlobalMessage createGlobalRequestMessageUpdate(int destinationId, String fileName, String requestId, int chunkId, ByteString data, int totalCount) {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		
		File.Builder fb = File.newBuilder();
	    fb.setChunkId(chunkId);
	    fb.setData(data);
	    fb.setFilename(fileName);
	    fb.setTotalNoOfChunks(totalCount);
		
	    Request.Builder rb = Request.newBuilder();
	    rb.setRequestId(requestId);
	    rb.setRequestType(RequestType.UPDATE);
	    
	    rb.setFile(fb);
	    GlobalMessage.Builder gb = GlobalMessage.newBuilder();
	    gb.setGlobalHeader(header);
	    gb.setRequest(rb);    
	    return gb.build();
	}
	public static GlobalMessage createGlobalRequestMessageDelete(String filename,String requestId) {
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		header.setTime(System.currentTimeMillis());
		header.setDestinationId(GlobalEdgeMonitor.state.getgConf().getClusterId());
		
	    Request.Builder rb = Request.newBuilder();
	    rb.setRequestId(requestId);
	    rb.setRequestType(RequestType.DELETE);
	    rb.setFileName(filename);
	    
	    GlobalMessage.Builder gb = GlobalMessage.newBuilder();
	    gb.setGlobalHeader(header);
	    gb.setRequest(rb);    
	    return gb.build();
	}
}
