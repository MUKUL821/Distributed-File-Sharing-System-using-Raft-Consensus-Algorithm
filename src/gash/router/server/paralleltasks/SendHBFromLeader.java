package gash.router.server.paralleltasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.work.Work.WorkMessage;

public class SendHBFromLeader implements Runnable {
	private static Logger logger = LoggerFactory.getLogger("SendHBFromLeader");
	EdgeInfo ei;
	public EdgeInfo getEi() {
		return ei;
	}
	public void setEi(EdgeInfo ei) {
		this.ei = ei;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try
		{
		if (ei.isActive() && ei.getChannel() != null) {
			if(!ei.getChannel().isActive()){
				ei.setActive(false);
				ei.setChannel(null);
				EdgeList.map.put(ei.getRef(), ei);
			}
			else {
				WorkMessage wm = WorkMessageUtils.createHB(ei);
				ei.getChannel().writeAndFlush(wm);
			}
		} else {
			Channel channel;
			Bootstrap b = new Bootstrap();
			b.group(new NioEventLoopGroup());
			b.channel(NioSocketChannel.class);
			b.handler(new WorkInit(EdgeMonitor.state,false));
			b.option(ChannelOption.SO_KEEPALIVE, true);
			//logger.info("Trying to connect to "+ei.getHost());
			channel=null;
			try{
				channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
				ei.setChannel(channel);
				ei.setActive(true);
				//logger.info("connected to node " + ei.getRef());
				WorkMessage wm = WorkMessageUtils.createHB(ei);
				ei.getChannel().writeAndFlush(wm);
			}
			catch(Exception e1){
				logger.error("Unable to connect to "+ei.getHost());
			}
		}
		}
		catch(Exception e)
		{
			logger.error("Error While hb is sent from leader : " + e.getMessage());
		}
	}
	

}