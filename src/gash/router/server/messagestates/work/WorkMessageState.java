package gash.router.server.messagestates.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.WorkHandler;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public abstract class WorkMessageState {
	protected static Logger logger = LoggerFactory.getLogger("WorkMessageState");
	protected ChannelHandlerContext ctx;
	protected WorkMessage msg;
	protected WorkHandler workHandler;
	public abstract void execute();
}
