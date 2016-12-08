package gash.router.server.messagestates.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.CommandHandler;
import io.netty.channel.ChannelHandlerContext;
import routing.Pipe.CommandMessage;

public abstract class CommandMessageState {
	protected static Logger logger = LoggerFactory.getLogger("CommandMessageState");
	protected ChannelHandlerContext ctx;
	protected CommandMessage msg;
	protected CommandHandler commandHandler;
	public abstract void execute();
}
