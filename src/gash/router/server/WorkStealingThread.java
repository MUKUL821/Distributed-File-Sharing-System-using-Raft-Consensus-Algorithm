package gash.router.server;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.ServerState;
import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.utils.ElectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;

import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;
import pipe.work.Work.WorkStealingRequest;

public class WorkStealingThread implements Runnable{
	private static Logger logger = LoggerFactory.getLogger("WorkStealingThread");
	private ServerState state;
	public WorkStealingThread(){
		logger.info("Starting WorkStealing Thread");
		state = ElectionUtil.state;
	}
	
	public WorkMessage createWorkStealingRequest(EdgeInfo ei){
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);
		
	//	Heartbeat.Builder bb = Heartbeat.newBuilder();
        WorkStealingRequest.Builder bb = WorkStealingRequest.newBuilder();
        bb.setNodeId(0);//set the node id value of the current server
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setSecret(100);
		wb.setHeader(hb);
		wb.setWorkStealingRequest(bb);

		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	
	public boolean isBusy(){
		//Check if the queue has more than 3 values
		try{
		if(FileRequestContainer.readRequestQueue.size()>2){
			return true;
		}
		return false;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return false;
		}
	}

	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(getRandomNum(15000,5000));
				if(!isBusy()&&ServerState.state.equals("Follower")){
					for (EdgeInfo ei : EdgeList.activeEdges.values()) {
						logger.info("Sending WorkStealing Request to Node:"+ei.getRef());
						if (ei.isActive() && ei.getChannel() != null) {
							WorkMessage wm = createWorkStealingRequest(ei);
							ei.getChannel().writeAndFlush(wm);
						} else {
							// TODO create a client to the node
	
							Channel channel;
							Bootstrap b = new Bootstrap();
							b.group(new NioEventLoopGroup());
							b.channel(NioSocketChannel.class);
							b.handler(new WorkInit(null,false));
							//logger.info("Trying to connect to "+ei.getHost());
							channel=null;
							try{
								channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
								ei.setChannel(channel);
								ei.setActive(true);
								logger.info("connected to node " + ei.getRef());
							}
							catch(Exception e){
								logger.error("Unable to connect to "+ei.getHost());
								
							}
						}
					}
				}
			} catch(Exception e){
				logger.error("Exception at Work Stealing Thread : " +e.getMessage());
			}
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
		
}