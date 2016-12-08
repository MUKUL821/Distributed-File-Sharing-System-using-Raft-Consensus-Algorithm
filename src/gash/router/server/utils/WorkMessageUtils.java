package gash.router.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.ChunkTransferHandler;
import gash.router.server.ServerState;
import gash.router.server.WorkHandler;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.edges.LeaderDetails;
import gash.router.server.messagestates.chunktransfer.HasChunkResponse;
import gash.router.server.messagestates.chunktransfer.HasFileUploadedInFollowerAck;
import gash.router.server.messagestates.work.HasBeatAckState;
import gash.router.server.messagestates.work.HasBeatState;
import gash.router.server.messagestates.work.HasChunkFileState;
import gash.router.server.messagestates.work.HasChunkResponseState;
import gash.router.server.messagestates.work.HasDeleteFileRequest;
import gash.router.server.messagestates.work.HasErrorState;
import gash.router.server.messagestates.work.HasPingState;
import gash.router.server.messagestates.work.HasStateState;
import gash.router.server.messagestates.work.HasTaskState;
import gash.router.server.messagestates.work.HasUpdateFileRequest;
import gash.router.server.messagestates.work.HasVoteRequestState;
import gash.router.server.messagestates.work.HasVoteResponseState;
import gash.router.server.messagestates.work.HasWorkSharingAckState;
import gash.router.server.messagestates.work.HasWorkSharingRequestState;
import gash.router.server.messagestates.work.HasWorkStealingRequestState;
import gash.router.server.messagestates.work.HasWorkStealingResponseState;
import global.Global.GlobalMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import pipe.common.Common.Header;
import pipe.work.Work.ChunkFile;
import pipe.work.Work.ChunkResponse;
import pipe.work.Work.DeleteFile;
import pipe.work.Work.FileUploadedAck;
import pipe.work.Work.FileUploadedAckFollower;
import pipe.work.Work.Heartbeat;
import pipe.work.Work.HeartbeatAck;
import pipe.work.Work.UpdateFile;
import pipe.work.Work.VoteRequest;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;
import routing.Pipe.CommandMessage;

public class WorkMessageUtils {
	
	private static Logger logger = LoggerFactory.getLogger("WorkMessageUtils");
	public static void setWorkMessageState(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler)
	{
		try{
		if (msg.hasBeat()) {
			workHandler.setMsgState(new HasBeatState(msg,ctx,workHandler));
		}
		else if(msg.hasChunkResponse())
		{
			workHandler.setMsgState(new HasChunkResponseState(msg,ctx,workHandler));
		}
		else if(msg.hasBeatAck())
		{
			workHandler.setMsgState(new HasBeatAckState(msg, ctx, workHandler));
		}
		else if(msg.hasChunkFile())
		{
			workHandler.setMsgState(new HasChunkFileState(msg, ctx, workHandler));
		}
		else if(msg.hasChunkResponse())
		{
			workHandler.setMsgState(new HasChunkResponseState(msg, ctx, workHandler));
		}
		else if(msg.hasErr())
		{
			workHandler.setMsgState(new HasErrorState(msg, ctx, workHandler));
		}
		else if(msg.hasPing())
		{
			workHandler.setMsgState(new HasPingState(msg, ctx, workHandler));
		}
		else if(msg.hasTask())
		{
			workHandler.setMsgState(new HasTaskState(msg, ctx, workHandler));
		}
		else if(msg.hasVoteRequest())
		{
			workHandler.setMsgState(new HasVoteRequestState(msg, ctx, workHandler));
		}
		else if(msg.hasVoteResponse())
		{
			workHandler.setMsgState(new HasVoteResponseState(msg, ctx, workHandler));
		}
		else if(msg.hasWorkSharingRequestAck())
		{
			workHandler.setMsgState(new HasWorkSharingAckState(msg, ctx, workHandler));
		}
		else if(msg.hasWorkSharingRequest())
		{
			workHandler.setMsgState(new HasWorkSharingRequestState(msg, ctx, workHandler));
		}
		else if(msg.hasWorkStealingRequest())
		{
			workHandler.setMsgState(new HasWorkStealingRequestState(msg, ctx, workHandler));
		}
		else if(msg.hasWorkStealingResponse())
		{
			workHandler.setMsgState(new HasWorkStealingResponseState(msg, ctx, workHandler));
		}
		else if (msg.hasState()) {
				workHandler.setMsgState(new HasStateState(msg, ctx, workHandler));
		}
		else if (msg.hasDeleteFile()) {
			workHandler.setMsgState(new HasDeleteFileRequest(msg, ctx, workHandler));
		}
		else if (msg.hasUpdateFile()) {
			workHandler.setMsgState(new HasUpdateFileRequest(msg, ctx, workHandler));
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	public static void setChunkTransferMessageState(WorkMessage msg, Channel channel, ChunkTransferHandler chunkTransferHandler)
	{
		try{
		if(msg.hasChunkResponse())
		{
			chunkTransferHandler.setMsgState(new HasChunkResponse(msg, channel));
		}
		else if(msg.hasFileUploadedAckFollower())
		{
			chunkTransferHandler.setMsgState(new HasFileUploadedInFollowerAck(msg, channel));
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	public static CommandMessage toCommandMsg(WorkMessage msg) {
		try{
	    routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
		routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
		routing.Pipe.Request.Builder req= routing.Pipe.Request.newBuilder();
		hb.setClusterId(999);
	 	hb.setTime(System.currentTimeMillis());
	 	req.setRequestId("100");
		req.setRequestType(routing.Pipe.RequestType.WRITE);
		routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
	    file.setFilename(msg.getChunkFile().getFileName());
	    file.setChunkCount(msg.getChunkFile().getChunkCount());
	    file.setChunkId(msg.getChunkFile().getChunkId());
	    file.setData(msg.getChunkFile().getData());
	    req.setFile(file.build());
	    
		rb.setGlobalHeader(hb.build());
		rb.setRequest(req.build());
		return rb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	
	public static WorkMessage createChunkAck(CommandMessage msg, ServerState state) {
		// TODO Auto-generated method stub
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		ChunkResponse.Builder bb = ChunkResponse.newBuilder();
		bb.setChunkCount(msg.getRequest().getFile().getChunkCount());
		bb.setChunkId(msg.getRequest().getFile().getChunkId());
		bb.setFileName(msg.getRequest().getFile().getFilename());
		bb.setIsSuccess(true);

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setChunkResponse(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	
	public static WorkMessage createFileTransferAck(String filename,ServerState state) {
		// TODO Auto-generated method stub
		try{
		logger.info("Creating file transfer ack");
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		FileUploadedAck.Builder bb = FileUploadedAck.newBuilder();
		bb.setFileName(filename);
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setFileUploadedAck(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	
	public static WorkMessage createFileTransferAckFollower(String filename, ServerState state) {
		// TODO Auto-generated method stub
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		FileUploadedAckFollower.Builder bb = FileUploadedAckFollower.newBuilder();
		bb.setFileName(filename);
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setFileUploadedAckFollower(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	public static WorkMessage createHB(EdgeInfo ei) {
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		Heartbeat.Builder bb = Heartbeat.newBuilder();
		bb.setState(sb);
		bb.setId(EdgeMonitor.state.getConf().getNodeId());
		bb.setTerm(LeaderDetails.term);
		bb.setServerState(ServerState.state);

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setBeat(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	
	public static WorkMessage getHeartBeatAck(EdgeInfo ei) {
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);
		
		HeartbeatAck.Builder bb = HeartbeatAck.newBuilder();
		bb.setTerm(LeaderDetails.term);
		bb.setServerState(ServerState.state);
		
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setBeatAck(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}

	public static WorkMessage createVoteRequest(EdgeInfo ei , ServerState state){	
		try{
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		
		
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		VoteRequest.Builder vm = VoteRequest.newBuilder();
		vm.setId(state.getConf().getNodeId());
		//LeaderDetails.setTerm(LeaderDetails.getTerm()+1);
		vm.setTerm(LeaderDetails.getTerm());
		wm.setVoteRequest(vm.build());

		wm.setHeader(hb);
		wm.setSecret(100);
		logger.info(" created voterequest with term"+LeaderDetails.getTerm());
		return wm.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}

	public static WorkMessage createDeleteMessage(String fileName) {
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		
		
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		DeleteFile.Builder dm = DeleteFile.newBuilder();
		dm.setFileName(fileName);
		wm.setDeleteFile(dm.build());

		wm.setHeader(hb);
		wm.setSecret(100);
		return wm.build();
	}

	public static WorkMessage createWorkUpdateRequest(CommandMessage msg) {
		// TODO Auto-generated method stub
		
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		
		
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		UpdateFile.Builder um = UpdateFile.newBuilder();
		um.setChunkId(msg.getRequest().getFile().getChunkId());
		um.setChunkCount(msg.getRequest().getFile().getChunkCount());
		um.setFileName(msg.getRequest().getFile().getFilename());
		um.setData(msg.getRequest().getFile().getData());
		wm.setUpdateFile(um.build());

		wm.setHeader(hb);
		wm.setSecret(100);
		return wm.build();
	}
	public static WorkMessage createWorkUpdateRequest(GlobalMessage msg) {
		// TODO Auto-generated method stub
		
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		
		
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		UpdateFile.Builder um = UpdateFile.newBuilder();
		um.setChunkId(msg.getRequest().getFile().getChunkId());
		um.setChunkCount(msg.getRequest().getFile().getTotalNoOfChunks());
		um.setFileName(msg.getRequest().getFile().getFilename());
		um.setData(msg.getRequest().getFile().getData());
		wm.setUpdateFile(um.build());

		wm.setHeader(hb);
		wm.setSecret(100);
		return wm.build();
	}

	public static WorkMessage createWorkWriteRequest(GlobalMessage msg) {
		// TODO Auto-generated method stub
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(EdgeMonitor.state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		
		
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		ChunkFile.Builder um = ChunkFile.newBuilder();
		um.setChunkId(msg.getRequest().getFile().getChunkId());
		um.setChunkCount(msg.getRequest().getFile().getTotalNoOfChunks());
		um.setFileName(msg.getRequest().getFile().getFilename());
		um.setData(msg.getRequest().getFile().getData());
		wm.setChunkFile(um);

		wm.setHeader(hb);
		wm.setSecret(100);
		return wm.build();
	}
}
