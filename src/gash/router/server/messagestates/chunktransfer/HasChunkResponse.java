package gash.router.server.messagestates.chunktransfer;

import java.util.HashSet;

import gash.router.server.paralleltasks.ChunkTransferThread;
import io.netty.channel.Channel;
import pipe.work.Work.ChunkResponse;
import pipe.work.Work.WorkMessage;

public class HasChunkResponse extends ChunkTransferState {
	public HasChunkResponse(WorkMessage msg, Channel channel) {
		this.msg = msg;
		this.channel = channel;
	}
	@Override
	public void execute() {
		try{
		logger.info("received chunk response inside Chunkhandler for id: "+msg.getChunkResponse().getChunkId()+" from node "+msg.getHeader().getNodeId());
		ChunkResponse ch = msg.getChunkResponse();
		String fileName = ch.getFileName();
		if(!ChunkTransferThread.chunkResponseList.containsKey(fileName)){
			HashSet<Integer> hs = new HashSet<Integer>();
			hs.add(ch.getChunkId());
			ChunkTransferThread.chunkResponseList.put(fileName,hs);
		}
		else {
			ChunkTransferThread.chunkResponseList.get(fileName).add(ch.getChunkId());
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
		
	}
	
}
