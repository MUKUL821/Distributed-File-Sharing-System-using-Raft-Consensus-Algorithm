package gash.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.messagestates.global.GlobalMessageState;
import gash.router.server.messagestates.work.WorkMessageState;
import gash.router.server.utils.GlobalMessageUtils;
import gash.router.server.utils.WorkMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;

public class GlobalHandler extends SimpleChannelInboundHandler<GlobalMessage> {
	public static Logger logger = LoggerFactory.getLogger("global");
	protected ServerState state;
	protected boolean debug = false;
	protected GlobalMessageState msgState;

	public GlobalHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 * @param ctx 
	 */
	public void handleMessage(GlobalMessage msg, Channel channel, ChannelHandlerContext ctx) {
		try{	
		if (msg == null) {
			logger.error("ERROR: Unexpected content - Message is null");
			return;
		}
	
		try {
			GlobalMessageUtils.setGlobalMessageState(msg, ctx, this);
			msgState.execute();
		} 
		
		catch (Exception e) {
			// TODO add logging
			logger.error("Exception occurred : " + e.getMessage());
			
		}
		
		System.out.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
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
	protected void channelRead0(ChannelHandlerContext ctx, GlobalMessage msg) throws Exception {
		System.out.println("Inside channel read");
		handleMessage(msg, ctx.channel(),ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		try{
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	
	public GlobalMessageState getMsgState() {
		return msgState;
	}

	public void setMsgState(GlobalMessageState msgState) {
		this.msgState = msgState;
	}
	
	public ServerState getState() {
		return state;
	}

	public void setState(ServerState state) {
		this.state = state;
	}
}
