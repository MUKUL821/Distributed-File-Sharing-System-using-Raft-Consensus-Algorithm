package gash.router.server.messagestates.command;

import gash.router.server.CommandHandler;
import io.netty.channel.ChannelHandlerContext;
import routing.Pipe.CommandMessage;

public class HasPingState extends CommandMessageState{

	 public HasPingState(CommandMessage msg, ChannelHandlerContext ctx , CommandHandler commandHandler) {
		// TODO Auto-generated constructor stub
		 this.msg = msg;
		 this.ctx = ctx;
		 this.commandHandler = commandHandler;
	}
	 
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		logger.info("RECEIVED PING FROM :"+ctx.channel().remoteAddress().toString());
	}

}
