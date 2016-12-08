package gash.router.server.messagestates.work;

import gash.router.server.MessageServer;
import gash.router.server.WorkHandler;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.channel.ChannelHandlerContext;
import pipe.work.Work.WorkMessage;

public class HasDeleteFileRequest extends WorkMessageState{
	public HasDeleteFileRequest(WorkMessage msg, ChannelHandlerContext ctx, WorkHandler workHandler) {
		this.msg = msg;
		this.ctx = ctx;
		this.workHandler = workHandler;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		logger.info("RECEIVED Delete FROM :"+ctx.channel().remoteAddress().toString());
		String fileName = msg.getDeleteFile().getFileName();
		boolean check = MessageServer.StartWorkCommunication.getInstance().check(fileName);
		if(check){
			try{
				MongoDBHandler mongo = new MongoDBHandler();
				GuavaHandler guava = new GuavaHandler();
				MessageServer.StartWorkCommunication.getInstance().delete(fileName,mongo,guava, ctx);
			}
			   catch(Exception e){
				   System.out.println("Caught exception during delete file operation");
			}				
		}
	}

}
