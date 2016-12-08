package gash.router.server.paralleltasks;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.FileReadRequestObject;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.utils.ElectionUtil;
import io.netty.channel.ChannelHandlerContext;
import pipe.common.Common.Header;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkSharingRequest;
import pipe.work.Work.WorkState;

public class DBRequestFollowersThread {
	
	private static Logger logger = LoggerFactory.getLogger("DBRequestFollowersThread");
	public static volatile ConcurrentHashMap<String,FileReadRequestObject> fileRequestClientMap = new ConcurrentHashMap<>();

	public static boolean sendWorkSharingRequest(String requestId, EdgeInfo ei, String fileName, ChannelHandlerContext ctx) {
		try{
		logger.info("Send Work Sharing Request");
		FileReadRequestObject f = new FileReadRequestObject();
		f.setFileName(fileName);
		f.setCtx(ctx);
		f.setNodeId(ei.getRef());
		fileRequestClientMap.put(requestId, f);
		boolean flag=false;
		boolean success = false;
		long waitTime = System.currentTimeMillis()+5000;
		while(!flag&& System.currentTimeMillis() < waitTime){
			//send WorkSharingRequest to the ei node
			WorkState.Builder sb = WorkState.newBuilder();
			sb.setEnqueued(-1);
			sb.setProcessed(-1);

			WorkSharingRequest.Builder bb = WorkSharingRequest.newBuilder();
			bb.setFileName(fileName);
			bb.setRequestId(requestId);
			
			Header.Builder hb = Header.newBuilder();
			hb.setNodeId(ElectionUtil.state.getConf().getNodeId());
			hb.setDestination(2);
			hb.setTime(System.currentTimeMillis());
			

			WorkMessage.Builder wb = WorkMessage.newBuilder();
			wb.setHeader(hb);
			wb.setWorkSharingRequest(bb);
			wb.setSecret(100);
			logger.info("Generated requestId:"+requestId+" for the work transfer to Node:"+f.getNodeId());
			ei.getChannel().writeAndFlush(wb.build());
			FileReadRequestObject frro = fileRequestClientMap.get(requestId);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(frro != null && frro.isReceivedAck()){
				flag=true;
				success=fileRequestClientMap.get(requestId).isResult();
			}
			
		}
		if(success==false)
			fileRequestClientMap.remove(requestId);
		return success;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return false;
		}
	}

}
