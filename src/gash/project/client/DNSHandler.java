package gash.project.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.work.Work.WorkMessage;

public class DNSHandler extends SimpleChannelInboundHandler<WorkMessage>{

	private static Logger logger = LoggerFactory.getLogger("DNSHandler");
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		// TODO Auto-generated method stub
		if(msg.hasDnsResponse())
		{
			System.out.println("inside Client DNS handler");
			Client.leaderHost= msg.getDnsResponse().getHost();
			System.out.println(Client.leaderHost);
			Client.leaderPort = 4568;
			logger.info("leader host" + Client.leaderHost);
			ctx.channel().close();
		}
			
	}

}
