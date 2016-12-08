package gash.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.messagestates.chunktransfer.ChunkTransferState;
import gash.router.server.utils.PrintUtil;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;
import pipe.work.Work.WorkMessage;

public class ChunkTransferHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected static Logger logger = LoggerFactory.getLogger("ChunkTransferHandler");
	protected ServerState state;
	protected boolean debug = false;
	protected ChunkTransferState msgState;


	public ChunkTransferHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	public void handleMessage(WorkMessage msg, Channel channel) {
		try{
		if (msg == null) {
			logger.error("ERROR: Unexpected content - Null Message received");
			return;
		}

		if (debug)
			PrintUtil.printWork(msg);

		// TODO How can you implement this without if-else statements?
		try {
			WorkMessageUtils.setChunkTransferMessageState(msg, channel, this);
			msgState.execute();
		} 
		catch (Exception e) {
			// TODO add logging
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}

		System.out.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
		try{
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}
	
	public ChunkTransferState getMsgState() {
		return msgState;
	}

	public void setMsgState(ChunkTransferState msgState) {
		this.msgState = msgState;
	}

}
