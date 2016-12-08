package gash.router.server.messagestates.command;

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

public class HasUpdateRequestState extends CommandMessageState{

	 public HasUpdateRequestState(CommandMessage msg, ChannelHandlerContext ctx , CommandHandler commandHandler) {
		// TODO Auto-generated constructor stub
		 this.msg = msg;
		 this.ctx = ctx;
		 this.commandHandler = commandHandler;
	}
	 
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		if(ServerState.state.equals("Leader")){
			String fileName = msg.getRequest().getFile().getFilename();
			boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
			if(check){
				try{
					MongoDBHandler mongo = new MongoDBHandler();
					GuavaHandler guava = new GuavaHandler();
					MessageServer.StartWorkCommunication.getInstance().update(msg,mongo,guava, ctx);
				}catch(Exception e){
				    System.out.println("Caught exception during Updaterequest");
				}	
			}
			WorkMessage wm = WorkMessageUtils.createWorkUpdateRequest(msg);
			EdgeMonitor.broadCastWorkMessage(wm);
			GlobalMessage gm =GlobalMessageUtils.createGlobalRequestMessageUpdate(GlobalEdgeMonitor.state.getgConf().getClusterId(),msg.getRequest().getFile().getFilename(),msg.getRequest().getFile().getFilename(),msg.getRequest().getFile().getChunkId(),msg.getRequest().getFile().getData(),msg.getRequest().getFile().getChunkCount());
			GlobalEdgeMonitor.sendGlobalMessageAllNeighbours(gm);
		}
	}

}
