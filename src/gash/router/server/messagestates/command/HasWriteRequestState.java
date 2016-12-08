package gash.router.server.messagestates.command;

import java.util.UUID;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.CommandHandler;
import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.utils.GlobalMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;
import routing.Pipe.CommandMessage;

public class HasWriteRequestState extends CommandMessageState {
	 public HasWriteRequestState(CommandMessage msg, ChannelHandlerContext ctx , CommandHandler commandHandler) {
		// TODO Auto-generated constructor stub
		 this.msg = msg;
		 this.ctx = ctx;
		 this.commandHandler = commandHandler;
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try
		{
		if(ServerState.state.equals("Leader")){
			MongoDBHandler mongo = new MongoDBHandler();
			GuavaHandler guava = new GuavaHandler();
			MessageServer.StartWorkCommunication.getInstance().upload(msg,mongo,guava, ctx);
		//	msg.getRequest().getFile().
			//Store in local db
			
			
	        //		channelMap.put(msg.getGlobalHeader().getClusterId(),channel);
	        //		MessageServer.StartWorkCommunication.getInstance().store(channelMap,msg);
			
			//Transfer to other servers within the network
			////LATER.......transferToWork(msg);
	
/***sending to mongodb***/

			String requestId = UUID.randomUUID().toString();
			GlobalMessage gm =GlobalMessageUtils.createGlobalRequestMessageWrite(GlobalEdgeMonitor.state.getgConf().getClusterId(),msg.getRequest().getFile().getFilename(),requestId,msg.getRequest().getFile().getChunkId(),msg.getRequest().getFile().getData(),msg.getRequest().getFile().getChunkCount());
			GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
			}
		}
		catch(Exception e)
		{
			logger.error("Error While wring to Db / cache : " + e.getMessage());
		}
	}

}
