package gash.router.dns.server;

import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Header;
import pipe.work.Work.DNSResponse;
import pipe.work.Work.UpdateFile;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

public class DNSServerHandler extends SimpleChannelInboundHandler<WorkMessage> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		// will get all the msgs here on req
		onMessage(ctx,msg);
		
	}

	private void onMessage(ChannelHandlerContext ctx, WorkMessage msg) {
		 System.out.println("Inside DNS server handler" + msg.hasDnsRequest());
		if(msg.getBeat().getServerState().equals("Leader")){
			System.out.println("Inside if");
				System.out.println("Leader Details found");
				int nodeId = msg.getBeat().getId();
				EdgeInfo ei1 = new EdgeInfo(1,"192.100.100.102",4568);
				EdgeList.map.put(1,ei1);
				ei1 = new EdgeInfo(5,"192.100.100.101",4568);

				EdgeList.map.put(5,ei1);
				ei1 = new EdgeInfo(2,"192.100.100.100",4568);

				EdgeList.map.put(2,ei1);
				EdgeInfo ei = EdgeList.map.get(nodeId);
				
				//change to workmessage
				//routing.Pipe.CommandMessage.Builder cm = routing.Pipe.CommandMessage.newBuilder();
				
				Header.Builder hb = Header.newBuilder();
				hb.setNodeId(10);
				hb.setDestination(2);
				hb.setTime(System.currentTimeMillis());
				
				
				//WorkMessage.Builder wm = WorkMessage.newBuilder();
				DNSResponse.Builder bb = DNSResponse.newBuilder();
				bb.setHost(ctx.channel().remoteAddress().toString().substring(1).split(":")[0]);
				bb.setPort("4568");
				DNSServer.wm.setDnsResponse(bb);

				DNSServer.wm.setHeader(hb);
				DNSServer.wm.setSecret(100);
				
				
				/*
				
				WorkState.Builder sb = WorkState.newBuilder
						();
				sb.setEnqueued(-1);
				sb.setProcessed(-1);
				
				DNSResponse.Builder bb = DNSResponse.newBuilder();
				System.out.println(ctx.channel().remoteAddress().toString().substring(1).split(":")[0]);
				
				bb.setHost(ctx.channel().remoteAddress().toString().substring(1).split(":")[0]);
				bb.setPort("4568");

				Header.Builder hb = Header.newBuilder();
				hb.setNodeId(msg.getHeader().getNodeId());
				hb.setDestination(2);
				hb.setTime(System.currentTimeMillis());
				

				WorkMessage.Builder wb = WorkMessage.newBuilder();
				wb.setHeader(hb);
				wb.setDnsResponse(bb);
				wb.setSecret(100);
				DNSServer.workBuilder = wb;
			//ctx.channel().writeAndFlush(cm.build());
				*/
			}
		//client req
	else if(msg.hasDnsRequest())
		{
		//
			System.out.println(DNSServer.wm.getDnsResponse().getHost());
			ctx.channel().writeAndFlush(DNSServer.wm.build());
		}
		}
		
	}


