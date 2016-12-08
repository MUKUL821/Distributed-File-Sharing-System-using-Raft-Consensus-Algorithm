package gash.router.server.paralleltasks;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.ChunkTransferInit;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;
import pipe.work.Work.ChunkFile;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;
import routing.Pipe.CommandMessage;

public class ChunkTransferThread implements Runnable {
	private static Logger logger = LoggerFactory.getLogger("ChunkTransferThread");
	
	public static volatile ConcurrentHashMap <String,HashSet<Integer>> chunkResponseList = new ConcurrentHashMap<String,HashSet<Integer>>();
	CommandMessage msg=null;
	GlobalMessage gMsg=null;
	EdgeInfo ei;
	public static volatile ConcurrentHashMap<Integer, EdgeInfo> hostList = new ConcurrentHashMap<Integer, EdgeInfo>();
	ServerState state;
	public ChunkTransferThread(CommandMessage msg, GlobalMessage gMsg, EdgeInfo ei, ServerState state) {
		try{
		this.msg = msg;
		this.gMsg= gMsg;
		this.ei = ei;
		this.state = state;
		if(!hostList.contains(ei.getRef()))
			hostList.put(ei.getRef(),new EdgeInfo(ei.getRef(), ei.getHost(), ei.getPort()));
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	@Override
	public void run() {
		try{
		boolean flag =true;
		EdgeInfo eiChunks = hostList.get(ei.getRef());
		while(flag){
			if (eiChunks.isActive() && eiChunks.getChannel() != null) {

				if(!eiChunks.getChannel().isActive()){
					eiChunks.setActive(false);
					eiChunks.setChannel(null);
					EdgeList.map.put(eiChunks.getRef(), eiChunks);
				}
				else {
					logger.info("Sending chunk to node "+ei.getRef());
					WorkMessage wm;
					if(msg!=null)
						wm = createChunkMessage(msg);
					else
						wm = createChunkMessage(gMsg);
					
					eiChunks.getChannel().writeAndFlush(wm);

					try {
						logger.info("sleeping for 3000ms");
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				Channel channel;
				Bootstrap b = new Bootstrap();
				b.group(new NioEventLoopGroup());
				b.channel(NioSocketChannel.class);
				b.handler(new ChunkTransferInit(state,false));
				b.option(ChannelOption.SO_KEEPALIVE, true);
			//	logger.info("Trying to connect to "+ei.getHost());
				channel=null;
				try{
					channel = b.connect(eiChunks.getHost(), eiChunks.getPort()).sync().channel();
					eiChunks.setChannel(channel);
					eiChunks.setActive(true);
					hostList.put(eiChunks.getRef(), ei);
					WorkMessage wm;
					if(msg!=null)
						wm = createChunkMessage(msg);
					else
						wm = createChunkMessage(gMsg);
					
					//System.out.println(wm);
					eiChunks.getChannel().writeAndFlush(wm);

					logger.info("Connected to node " + eiChunks.getRef());
				}catch(Exception e1){
					logger.error("Unable to connect to "+eiChunks.getHost());
					logger.error(e1.getMessage());
					continue;
				}
			}
			if(chunkResponseList.containsKey(msg.getRequest().getFile().getFilename())){
				HashSet<Integer> chunkId= chunkResponseList.get(msg.getRequest().getFile().getFilename());
				if(chunkId.contains(msg.getRequest().getFile().getChunkId()))
					flag=false;
			}
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	
}
	/*
	private ChunkResponse createChunkResponseMessage(CommandMessage msg){
		ChunkResponse.Builder bb = ChunkResponse.newBuilder();
		bb.setChunkCount(msg.getRequest().getFile().getChunkCount());
		bb.setChunkId(msg.getRequest().getFile().getChunkId());
		bb.setFileName(msg.getRequest().getFile().getFilename());
		bb.setIsSuccess(true);
		return bb.build();		
	}
	*/
	
	private WorkMessage createChunkMessage(CommandMessage msg) {
		// TODO Auto-generated method stub
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		ChunkFile.Builder bb = ChunkFile.newBuilder();
		bb.setChunkCount(msg.getRequest().getFile().getChunkCount());
		bb.setChunkId(msg.getRequest().getFile().getChunkId());
		bb.setData(msg.getRequest().getFile().getData());
		bb.setFileName(msg.getRequest().getFile().getFilename());

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setChunkFile(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
	private WorkMessage createChunkMessage(GlobalMessage msg) {
		// TODO Auto-generated method stub
		try{
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);
		System.out.println(msg.getRequest().getFile().getTotalNoOfChunks());
		System.out.println(msg.getRequest().getFile().getTotalNoOfChunks());
		ChunkFile.Builder bb = ChunkFile.newBuilder();
		bb.setChunkCount(msg.getRequest().getFile().getTotalNoOfChunks());
		bb.setChunkId(msg.getRequest().getFile().getChunkId());
		bb.setData(msg.getRequest().getFile().getData());
		bb.setFileName(msg.getRequest().getFile().getFilename());

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(2);
		hb.setTime(System.currentTimeMillis());
		

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setChunkFile(bb);
		wb.setSecret(100);
		return wb.build();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	}
}
