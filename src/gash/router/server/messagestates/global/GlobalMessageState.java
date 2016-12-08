package gash.router.server.messagestates.global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;

public abstract class GlobalMessageState {
	
	protected Logger logger = LoggerFactory.getLogger("GlobalMessageState");
	
	protected ChannelHandlerContext ctx;
	protected GlobalMessage msg;
	
	
	public abstract void execute();
}
