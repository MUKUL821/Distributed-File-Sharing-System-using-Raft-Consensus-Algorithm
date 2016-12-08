package gash.router.server.messagestates.command;

import java.util.UUID;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.CommandHandler;
import gash.router.server.FileReadRequestObject;
import gash.router.server.FileRequestContainer;
import gash.router.server.FileRequestObject;
import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.paralleltasks.DBRequestFollowersThread;
import gash.router.server.utils.GlobalMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;
import routing.Pipe.CommandMessage;

public class HasReadRequestState extends CommandMessageState {
	 public HasReadRequestState(CommandMessage msg, ChannelHandlerContext ctx , CommandHandler commandHandler) {
		// TODO Auto-generated constructor stub
		 this.msg = msg;
		 this.ctx = ctx;
		 this.commandHandler = commandHandler;
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			if(ServerState.state.equals("Leader")){
				logger.info("Read Request received from client");
				int queueSize = FileRequestContainer.readRequestQueue.size();
				String fileName = msg.getRequest().getFile().getFilename();
				boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
				
				// Sharing work in the else part where followers take charge in case queue size 
				if(check && queueSize<3) {//channelMap.put(msg.getGlobalHeader().getClusterId(),channel);
					logger.info("Retrieving file from Leader");
					FileRequestObject fro = new FileRequestObject(ctx, fileName, "command");
					// Adding all the read requests to the request queue
					FileRequestContainer.readRequestQueue.add(fro);
					
				} else {
					// Request the Followers for chunk
					// TODO Auto-generated method stub
					logger.info("Requesting file from Followers");
					boolean success = false;
					String requestId;
					while(!success){
						for(EdgeInfo ei : EdgeList.activeEdges.values()){
							requestId = UUID.randomUUID().toString();
						    success = DBRequestFollowersThread.sendWorkSharingRequest(requestId,ei,fileName,ctx);
							if(success)
									break;
						}
						if(success!=true){
							logger.info("File not found in cluster, requesting from next cluster");
							requestId = UUID.randomUUID().toString();
							FileReadRequestObject f = new FileReadRequestObject();
							f.setFileName(fileName);
							f.setCtx(ctx);
							f.setNodeId(GlobalEdgeMonitor.state.getgConf().getClusterId());
							DBRequestFollowersThread.fileRequestClientMap.put(requestId, f);
							GlobalMessage gm= GlobalMessageUtils.createGlobalRequestMessage(fileName, requestId);
							GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
							break;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error while reading files : " + e.getMessage());
		}
	}
}
