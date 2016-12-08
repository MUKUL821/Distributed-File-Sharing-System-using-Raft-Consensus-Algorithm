package gash.router.server.messagestates.chunktransfer;

import gash.router.server.MessageServer;
import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;

/* Ack Messages recieved from Follower to the Leader when the complete File is successfully uploaded
 * to the Follower
 */
public class HasFileUploadedInFollowerAck extends ChunkTransferState{
	public HasFileUploadedInFollowerAck(WorkMessage msg , Channel channel) {
		this.msg =msg;
		this.channel = channel;
	}
	@Override
	public void execute()
	{
		try{
		logger.info("received msg for FileUploadedAck from"+msg.getHeader().getNodeId());
		if(MessageServer.fileFollowerAckCountMap.containsKey(msg.getFileUploadedAckFollower().getFileName()))
		{
			int count = MessageServer.fileFollowerAckCountMap.get(msg.getFileUploadedAckFollower().getFileName())+1;
			MessageServer.fileFollowerAckCountMap.put(msg.getFileUploadedAckFollower().getFileName(), count);
		}
		else
		{
			MessageServer.fileFollowerAckCountMap.put(msg.getFileUploadedAckFollower().getFileName(), 1);
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
		
	}
}
