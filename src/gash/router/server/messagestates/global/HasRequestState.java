package gash.router.server.messagestates.global;

import java.net.UnknownHostException;
import java.util.UUID;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.FileRequestContainer;
import gash.router.server.FileRequestObject;
import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.paralleltasks.DBRequestFollowersThread;
import gash.router.server.utils.WorkMessageUtils;
import global.Global.GlobalMessage;
import global.Global.RequestType;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasRequestState extends GlobalMessageState {

	public HasRequestState(GlobalMessage msg , ChannelHandlerContext ctx) {
		// TODO Auto-generated constructor stub
		this.msg = msg;
		this.ctx = ctx;
	}
	@Override
	public void execute() {
		System.out.println("Leader");
		if(ServerState.state.equals("Leader")&&msg.getGlobalHeader().getDestinationId()!=GlobalEdgeMonitor.state.getgConf().getClusterId()){
			if(msg.getRequest().getRequestType().equals(RequestType.READ)){
				logger.info("Global Read Request received");
				String fileName = msg.getRequest().getFileName();			
				boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
				if(check){
					logger.info("Retrieving file from Leader reqId"+fileName+","+msg.getRequest().getRequestId());
					FileRequestObject fro = new FileRequestObject(null, fileName, "global");
					fro.setRequestId(msg.getRequest().getRequestId());
					fro.setDesitinationId(msg.getGlobalHeader().getDestinationId());
					// Adding all the read requests to the request queue
					FileRequestContainer.readRequestQueue.add(fro);
				} else {
					/*boolean success=false;
					for(EdgeInfo ei : EdgeList.activeEdges.values()){
						//requestId = UUID.randomUUID().toString();
						String requestId=msg.getRequest().getRequestId();
					    success = DBRequestFollowersThread.sendWorkSharingRequest(requestId,ei,fileName,null);
						if(success)
								break;
					}
					if(success==false){*/
					logger.info("File not found in cluster, requesting from next cluster");
					//forward filerequest to all neighbours
					GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
					//}
				}
			} else if(msg.getRequest().getRequestType().equals(RequestType.WRITE)){
				String fileName = msg.getRequest().getFile().getFilename();
				boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
			//	if(check){
			//		logger.info("File already exists in Cluster");
			//	} else {
					try{
						MongoDBHandler mongo = new MongoDBHandler();
						GuavaHandler guava = new GuavaHandler();
						MessageServer.StartGlobalCommunication.getInstance().upload(msg,mongo,guava, ctx);					
					} catch(Exception e) { 
						System.out.println("Caught exception during global file write");
					}
			//	}
				WorkMessage wm = WorkMessageUtils.createWorkWriteRequest(msg);
				EdgeMonitor.broadCastWorkMessage(wm);
				GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
				
			} else if(msg.getRequest().getRequestType().equals(RequestType.UPDATE)){
				String fileName = msg.getRequest().getFile().getFilename();
				boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
				if(check){
					try{
						MongoDBHandler mongo = new MongoDBHandler();
						GuavaHandler guava = new GuavaHandler();
						MessageServer.StartGlobalCommunication.getInstance().update(msg,mongo,guava, ctx);
					}catch(Exception e){
					    System.out.println("Caught exception during Updaterequest");
					}	
				}
				WorkMessage wm = WorkMessageUtils.createWorkUpdateRequest(msg);
				EdgeMonitor.broadCastWorkMessage(wm);			
				GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
			} else if(msg.getRequest().getRequestType().equals(RequestType.DELETE)){
				System.out.println("Received delete file request");
				String fileName = msg.getRequest().getFileName();
				boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
				System.out.println(check+","+fileName);
				if(check){
					MongoDBHandler mongo;
					try {
						mongo = new MongoDBHandler();
						GuavaHandler guava = new GuavaHandler();
						MessageServer.StartWorkCommunication.getInstance().delete(fileName,mongo,guava, ctx);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				WorkMessage wm = WorkMessageUtils.createDeleteMessage(fileName);
				EdgeMonitor.broadCastWorkMessage(wm);
				GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(msg);
			}
		}
		
	}

}
