package gash.router.server.paralleltasks;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.ServerState;
import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.edges.LeaderDetails;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;
import pipe.work.Work.HeartbeatAck;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

public 	class SendHBToLeader extends TimerTask{

private Logger logger = LoggerFactory.getLogger("SendHBtoLeader");

	public SendHBToLeader() {
	// TODO Auto-generated constructor stub
	
	}
	
	@Override
	public void run() {
		try {
			if("Follower".equals(ServerState.state) && LeaderDetails.isLeaderDetailsSet()){
				EdgeInfo ei = EdgeList.map.get(LeaderDetails.id);
				if (ei.isActive() && ei.getChannel() != null) {
					if(!ei.getChannel().isActive()){
						ei.setActive(false);
						ei.setChannel(null);
						EdgeList.map.put(ei.getRef(), ei);
					}
					else {
						WorkMessage wm = WorkMessageUtils.getHeartBeatAck(EdgeList.map.get(LeaderDetails.id));
						ei.getChannel().writeAndFlush(wm);
					}
				} else {
					Channel channel;
					Bootstrap b = new Bootstrap();
					b.group(new NioEventLoopGroup());
					b.channel(NioSocketChannel.class);
					b.handler(new WorkInit(EdgeMonitor.state,false));
					b.option(ChannelOption.SO_KEEPALIVE, true);
					logger.info("Trying to connect to 1"+ei.getHost());
					channel=null;
					try{
						channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
						ei.setChannel(channel);
						ei.setActive(true);
						logger.info("connected to node " + ei.getRef());
					}
					catch(Exception e1){
						logger.error("Unable to connect to "+ei.getHost());
					}
				}
			}
		}
		catch(Exception e)
		{
			EdgeInfo ei = EdgeList.map.get(LeaderDetails.id);
			ei.setActive(false);
			ei.setChannel(null);
			logger.error("Error while sending hb to leader: " + e.getMessage());
		}
	}
}
