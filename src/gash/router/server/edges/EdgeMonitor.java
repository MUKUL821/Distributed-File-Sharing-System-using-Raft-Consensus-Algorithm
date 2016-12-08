/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.edges;

import java.util.Random;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.container.RoutingConf.RoutingEntry;
import gash.router.server.ServerState;
import gash.router.server.WorkInit;
import gash.router.server.paralleltasks.ActiveNodesCheck;
import gash.router.server.paralleltasks.SendHBFromLeader;
import gash.router.server.paralleltasks.SendHBToLeader;
import gash.router.server.utils.ElectionUtil;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.work.Work.WorkMessage;



public class EdgeMonitor implements Runnable {
	public static Logger logger = LoggerFactory.getLogger("edge monitor");

	private EdgeList outboundEdges;
	private EdgeList inboundEdges;
	private long dt = 2000;
	public static ServerState state;
	
	public static final String dnsHost = "169.254.203.42";
	public static final int dnsPort = 4569;
	
	public EdgeMonitor(){
		
	}
	public EdgeMonitor(ServerState state) {
		try{
		if (state == null)
			throw new RuntimeException("state is null");

		ElectionUtil.state=state;
		this.outboundEdges = new EdgeList();
		this.inboundEdges = new EdgeList();
		EdgeMonitor.state = state;
		EdgeMonitor.state.setEmon(this);

		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				outboundEdges.addNode(e.getId(), e.getHost(), e.getPort());
			}
		}

		// cannot go below 2 sec
		if (state.getConf().getHeartbeatDt() > this.dt)
			this.dt = state.getConf().getHeartbeatDt();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

	public void createInboundIfNew(int ref, String host, int port) {
		try{
		inboundEdges.createIfNew(ref, host, port);
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

	public void shutdown() {
	}

	@Override
	public void run() {
		try
		{
		Timer timer = new Timer();
		timer.schedule(new ActiveNodesCheck(), 500, 500);
		timer.schedule(new SendHBToLeader(), 10, 10);
		while (true) {
			
			if(ServerState.state.equals("Leader")){
				EdgeInfo dnsEI= new EdgeInfo(10,dnsHost, dnsPort);
				SendHBFromLeader sbDNS = new SendHBFromLeader();
				sbDNS.setEi(dnsEI);
				Thread t1 = new Thread(sbDNS);
				t1.start();
				for (EdgeInfo ei : EdgeList.map.values()) {
					//logger.info("sending heartbeat");
					SendHBFromLeader sb = new SendHBFromLeader();
					sb.setEi(ei);
					Thread t = new Thread(sb);
					t.start();			
				}	
			} else if (ServerState.state.equals("Follower")){
				try {
					/* Election Time out */
					Thread.sleep(getRandomNum((((state.getConf().getNodeId())%4)+1)*600,(((state.getConf().getNodeId())%4)+1)*500));
					if(LeaderDetails.receivedHBFromLeader)
						LeaderDetails.receivedHBFromLeader=false;
					else {
						if(!ElectionUtil.isHappening && EdgeList.activeEdges.size()!=0){
							ElectionUtil.state=state;
							ServerState.state="Candidate";
							logger.info("The leader is down , starting election with Term: "+LeaderDetails.term+" !");
							ElectionUtil.startLeaderElection();
						}
						
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			} else if(ServerState.state.equals("Candidate")){
				//No operations here
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		}
		catch(Exception e)
		{
			logger.error("Error :" + e.getMessage());
		}
		
	}
	public int getRandomNum(int max, int min){
		try{
		Random rn = new Random();
		return rn.nextInt(max - min + 1) + min;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return -1;
		}
	}
	public static void broadCastWorkMessage(WorkMessage wm){
		for(EdgeInfo ei: EdgeList.map.values()){
			if (ei.isActive() && ei.getChannel() != null) {
				if(!ei.getChannel().isActive()){
					ei.setActive(false);
					ei.setChannel(null);
					EdgeList.map.put(ei.getRef(), ei);
				}
				else {
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
					ei.getChannel().writeAndFlush(wm);
				}
				catch(Exception e1){
					logger.error("Caught exception when sending WorkMessage to "+ei.getHost());
				}
			}
		}
	}

}
