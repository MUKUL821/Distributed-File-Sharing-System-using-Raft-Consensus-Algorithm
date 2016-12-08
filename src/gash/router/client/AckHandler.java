package gash.router.client;

import java.util.HashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.work.Work.WorkMessage;

public class AckHandler extends SimpleChannelInboundHandler<WorkMessage> {
static boolean messageReceived= false;
public static volatile HashMap<String, Boolean> ackMap = new HashMap<>();
	public AckHandler(){
		
	}

	@Override
	protected void channelRead0(ChannelHandlerContext cl, WorkMessage ack) throws Exception {
	if(ack.hasFileUploadedAck())
	{
		System.out.println("acknowledment received..");	
		ackMap.put(ack.getFileUploadedAck().getFileName(), true);
	}
	}
}
