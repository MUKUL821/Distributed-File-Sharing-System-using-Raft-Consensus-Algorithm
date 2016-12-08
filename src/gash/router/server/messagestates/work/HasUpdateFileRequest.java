package gash.router.server.messagestates.work;

import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.WorkHandler;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasUpdateFileRequest extends WorkMessageState{
	public HasUpdateFileRequest(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		String fileName = msg.getUpdateFile().getFileName();
		boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
		if(check){
			try{
				MongoDBHandler mongo = new MongoDBHandler();
				GuavaHandler guava = new GuavaHandler();
				MessageServer.StartWorkCommunication.getInstance().update(msg, mongo, guava, ctx);
			}catch(Exception e){
				System.out.println("Caught exception during Updaterequest");
			}	
		}
	}	

}
