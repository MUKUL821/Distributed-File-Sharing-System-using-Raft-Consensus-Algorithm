package gash.router.server.messagestates.command;

import java.util.UUID;

import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.CommandHandler;
import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.utils.GlobalMessageUtils;
import gash.router.server.utils.WorkMessageUtils;
import global.Global.GlobalMessage;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

public class HasDeleteRequestState extends CommandMessageState{

	 public HasDeleteRequestState(CommandMessage msg, ChannelHandlerContext ctx , CommandHandler commandHandler) {
		// TODO Auto-generated constructor stub
		 this.msg = msg;
		 this.ctx = ctx;
		 this.commandHandler = commandHandler;
	}
	 
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		if(ServerState.state.equals("Leader")){
			logger.info("RECEIVED Delete FROM :"+ctx.channel().remoteAddress().toString());
			String fileName = msg.getRequest().getFile().getFilename();
			System.out.println(fileName);
			boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
			if(check){
				try{
					MongoDBHandler mongo = new MongoDBHandler();
					GuavaHandler guava = new GuavaHandler();
					MessageServer.StartWorkCommunication.getInstance().delete(fileName,mongo,guava, ctx);
				}
				   catch(Exception e){
				   
				}				
			}
			WorkMessage wm = WorkMessageUtils.createDeleteMessage(fileName);
			EdgeMonitor.broadCastWorkMessage(wm);
			String requestId = UUID.randomUUID().toString();
			GlobalMessage gm= GlobalMessageUtils.createGlobalRequestMessageDelete(fileName, requestId);
			GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
		}
	}

}
