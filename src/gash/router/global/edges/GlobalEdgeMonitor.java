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
package gash.router.global.edges;

import java.util.Random;
import java.util.Timer;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.GlobalInit;
import gash.router.server.ServerState;
import gash.router.server.paralleltasks.ActiveNodesCheck;
import gash.router.server.paralleltasks.SendHBFromLeader;
import gash.router.server.paralleltasks.SendHBToLeader;
import gash.router.server.utils.GlobalMessageUtils;
import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import gash.router.container.GlobalRoutingConf.RoutingEntry;

public class GlobalEdgeMonitor implements Runnable {
	public static Logger logger = LoggerFactory.getLogger("edge monitor");

	private GlobalEdgeList outboundEdges;
	public static ServerState state;
	public GlobalEdgeMonitor(){
		
	}
	public GlobalEdgeMonitor(ServerState state) {
		try{
			if (state == null)
				throw new RuntimeException("state is null");
			this.outboundEdges = new GlobalEdgeList();
			GlobalEdgeMonitor.state = state;
			GlobalEdgeMonitor.state.setgEmon(this);
	
			if (state.getgConf().getRouting() != null) {
				for (RoutingEntry e : state.getgConf().getRouting()) {
					outboundEdges.addNode(e.getClusterId(), e.getHost(), e.getPort());
					System.out.println(e.getClusterId()+","+ e.getHost()+","+e.getPort());
				}
			}
			//System.out.println(outboundEdges.);
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

	@Override
	public void run() {
		try	{
			while(true){
				if(ServerState.state.equalsIgnoreCase("leader"))
		//	pingAllNeighbors();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		} finally {
			
		}
	}
	public void pingAllNeighbors(){
		for (GlobalEdgeInfo ei : GlobalEdgeList.map.values()) {
			if (ei.isActive() && ei.getChannel() != null) {
				System.out.println("Pinging "+ei.getRef());
				GlobalMessage gm = GlobalMessageUtils.createPing();
				ei.getChannel().writeAndFlush(gm);
			} else {
				Channel channel;
				Bootstrap b = new Bootstrap();
				b.group(new NioEventLoopGroup());
				b.channel(NioSocketChannel.class);
				b.handler(new GlobalInit(GlobalEdgeMonitor.state,false));
				b.option(ChannelOption.SO_KEEPALIVE, true);
				logger.info("Trying to connect to "+ei.getPort());
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
	/*
	public void sendWhoIsLeaderToNeighbor() {
		// TODO Auto-generated method stub
			for (GlobalEdgeInfo ei : GlobalEdgeList.map.values()) {
				if (ei.isActive() && ei.getChannel() != null) {
					if(!ei.getChannel().isActive()){
						ei.setActive(false);
						ei.setChannel(null);
						GlobalEdgeList.map.put(ei.getRef(), ei);
					}
					else {
						GlobalMessage gm = GlobalMessageUtils.createWhoIsLeader(ei);
						ei.getChannel().writeAndFlush(gm);
					}
				} else {
					Channel channel;
					Bootstrap b = new Bootstrap();
					b.group(new NioEventLoopGroup());
					b.channel(NioSocketChannel.class);
					b.handler(new GlobalInit(GlobalEdgeMonitor.state,false));
					b.option(ChannelOption.SO_KEEPALIVE, true);
					logger.info("Trying to connect to "+ei.getHost());
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
	*/
	public static void sendGlobalMessageAllNeighbours(GlobalMessage gm) {
		System.out.println(GlobalEdgeList.map);
		for (GlobalEdgeInfo ei : GlobalEdgeList.map.values()) {
			if (ei.isActive() && ei.getChannel() != null) {
				if(gm.hasRequest())
					System.out.println("sending file request message to "+ei.getHost());
				ei.getChannel().writeAndFlush(gm);
			} else {
				Channel channel;
				Bootstrap b = new Bootstrap();
				b.group(new NioEventLoopGroup());
				b.channel(NioSocketChannel.class);
				b.handler(new GlobalInit(GlobalEdgeMonitor.state,false));
				b.option(ChannelOption.SO_KEEPALIVE, true);
				logger.info("Trying to connect to "+ei.getHost());
				channel=null;
				try{
					channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
					ei.setChannel(channel);
					ei.setActive(true);
					logger.info("connected to node " + ei.getRef());
					ei.getChannel().writeAndFlush(gm);
				}
				catch(Exception e1){
					logger.error("Unable to connect to "+ei.getHost());
				}
			}
		}
		
	}

}
