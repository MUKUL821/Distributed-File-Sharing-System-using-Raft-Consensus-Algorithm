package gash.router.server.messagestates.chunktransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;

public abstract class ChunkTransferState {
	protected static Logger logger = LoggerFactory.getLogger("ChunkTransferState");
	protected WorkMessage msg;
	protected Channel channel;
	public abstract void execute();
}
