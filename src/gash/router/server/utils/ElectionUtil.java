package gash.router.server.utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.ServerState;
import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.LeaderDetails;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.work.Work.WorkMessage;
import pipe.common.Common.Header;
import pipe.work.Work.VoteRequest;
import pipe.work.Work.VoteResponse;
import java.lang.Thread;
public class ElectionUtil {
	private static Logger logger = LoggerFactory.getLogger("ElectionUtil");
	
	public volatile static boolean isHappening=false;
	public volatile static int totalVoteReceivedCount=0;
	public volatile static int voteYes=0;
	public volatile static int voteNo=0;
	public volatile static ConcurrentHashMap<Integer,EdgeInfo> notRespondedEdges = new ConcurrentHashMap<Integer,EdgeInfo>();
	public volatile static ServerState state;
	public volatile static int responseWaitTime;
	public volatile static ArrayList<Integer> hasVoted=new ArrayList<Integer>();
	public static void startLeaderElection(){
		try{
		isHappening= true;
		voteYes+=1;
		totalVoteReceivedCount+=1;
		LeaderDetails.setTerm(LeaderDetails.getTerm()+1);
		hasVoted.add(LeaderDetails.getTerm());
		notRespondedEdges = new ConcurrentHashMap<Integer,EdgeInfo>(EdgeList.map);
		ServerState.state="Candidate";
		if(isHappening== true&& ServerState.state.equals("Candidate")){
			broadCastVote(EdgeList.map);
			//responseWaitTime= e.getRandomNum(800,500);
			//int time = (int) (System.currentTimeMillis()+responseWaitTime);
			//boolean hasWon=false;
			//while(System.currentTimeMillis()<=time&&totalVoteReceivedCount!=(EdgeList.map.size()+1)&&notRespondedEdges.size()!=0){	
			/*try {
				Thread.sleep(20);
				broadCastVote(notRespondedEdges);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			boolean flag=false;
			int majoritySize = (EdgeList.map.size()/2)+1;
			int count=0;
			while(totalVoteReceivedCount<majoritySize&&count<2){	
			try {
					Thread.sleep(20);
					count+=1;
					if(isHappening== true&& ServerState.state=="Candidate"){
						broadCastVote(notRespondedEdges);
					}
					else {
						flag=true;
						break;
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			 
			}
			if(flag==false){
			//	logger.info(voteYes+","+(EdgeList.map.size()/2));
				if(voteYes>=majoritySize&&isHappening==true&&ServerState.state=="Candidate"){
				//if((voteYes>=(EdgeList.map.size()/2)+1)||hasWon==true){
					logger.info("Election over - declared Leader");
					LeaderDetails.receivedHBFromLeader=true;
					ServerState.state="Leader";
				} else {
					logger.info("Election over - Lost the election");
					ServerState.state="Follower";
				}	
			}
		} else {
			ServerState.state="Follower";
		//	logger.info("Election stopped - received another vote request");
		}
		isHappening=false;
		totalVoteReceivedCount=0;
		voteYes=0;
		voteNo=0;	
		EdgeList.activeEdges=new ConcurrentHashMap<>(EdgeList.map);
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

	public static void broadCastVote(ConcurrentHashMap<Integer, EdgeInfo> notRespondedEdges){
		try{
		ElectionUtil e= new ElectionUtil();
		for (EdgeInfo ei : notRespondedEdges.values()) {
			BroadCastVote br =e.new BroadCastVote();
			br.setEi(ei);
			Thread t = new Thread(br);
			t.start();			
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
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
	
	/* This method creates a vote response*/
	public static void createVoteResponse(VoteRequest voteRequest, EdgeInfo ei ) {
		try{
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		VoteResponse.Builder vr = VoteResponse.newBuilder();
		vr.setId(state.getConf().getNodeId());
		//set election happenning as true
		if(voteRequest.getTerm()>LeaderDetails.getTerm()&&!hasVoted.contains(voteRequest.getTerm())){
			ElectionUtil.isHappening=true;	
			ServerState.state="Follower";
			LeaderDetails.setTerm(voteRequest.getTerm());
			vr.setIsVoted(true);
			vr.setTerm(LeaderDetails.getTerm());
			hasVoted.add(voteRequest.getTerm());				
			EdgeList.activeEdges=new ConcurrentHashMap<>(EdgeList.map);
		}
		else{
			vr.setIsVoted(false);
			vr.setTerm(LeaderDetails.getTerm());
		}
		wm.setVoteResponse(vr.build());

		wm.setHeader(hb);
		wm.setSecret(100);
		WorkMessage msg= wm.build();		
		
		Channel channel;
		Bootstrap b = new Bootstrap();
		b.group(new NioEventLoopGroup());
		b.channel(NioSocketChannel.class);
		b.handler(new WorkInit(state,false));
		channel=null;
		try{
			channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
			ei.setChannel(channel);
			ei.setActive(true);
			logger.info("Sending vote response "+msg.getVoteResponse().getIsVoted()+"for term "+LeaderDetails.getTerm()+" to "+ei.getRef());
			//logger.info("connected to node " + ei.getRef());
		}catch(Exception e){
		logger.error("Exception Sending to send response to "+e.getMessage());
		}
		ei.getChannel().writeAndFlush(msg);

		ElectionUtil.isHappening=false;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	/* This method creates a response and send it */
	public static void sendResponse(WorkMessage msg, EdgeInfo ei) {
		// TODO Auto-generated method stub
		try{
		Channel channel;
		Bootstrap b = new Bootstrap();
		b.group(new NioEventLoopGroup());
		b.channel(NioSocketChannel.class);
		b.handler(new WorkInit(state,false));
		logger.info("Sending vote response to "+msg.getVoteResponse().getIsVoted()+","+ei.getHost());
		channel=null;
		try{
			channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
			ei.setChannel(channel);
			ei.setActive(true);
			//logger.info("connected to node " + ei.getRef());
		}catch(Exception e){
			logger.error("Unable to connect to "+ei.getHost());
		}
		ei.getChannel().writeAndFlush(msg);
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	public class BroadCastVote implements Runnable {
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
			try{
			if (ei.isActive() && ei.getChannel() != null && isHappening== true&& ServerState.state=="Candidate") {
				WorkMessage voteRequest = WorkMessageUtils.createVoteRequest(ei,state);
				ei.getChannel().writeAndFlush(voteRequest);
				logger.info("Sent vote request");		
			} else {
				Channel channel;
				Bootstrap b = new Bootstrap();
				b.group(new NioEventLoopGroup());
				b.channel(NioSocketChannel.class);
				b.handler(new WorkInit(state,false));
				logger.info("Requesting vote from "+ei.getHost());
				channel=null;
				try{
					channel = b.connect(ei.getHost(), ei.getPort()).sync().channel();
					ei.setChannel(channel);
					ei.setActive(true);

				
				}catch(Exception e){
					logger.error("Exception Occured while broadcasting vote : " + e.getMessage());
				}
			}
			}
			catch(Exception e)
			{
				logger.error("Exception Occurred : " + e.getMessage() );
			}
		}		
	}
}
