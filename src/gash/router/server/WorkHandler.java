/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.messagestates.work.WorkMessageState;
import gash.router.server.utils.WorkMessageUtils;
import io.netty.channel.Channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;

import pipe.work.Work.WorkMessage;


/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class WorkHandler extends SimpleChannelInboundHandler<WorkMessage> {
	public static Logger logger = LoggerFactory.getLogger("work");
	protected ServerState state;
	protected boolean debug = false;
	protected WorkMessageState msgState;

	public WorkHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 * @param ctx 
	 */
	public void handleMessage(WorkMessage msg, Channel channel, ChannelHandlerContext ctx) {
		try{
		if (msg == null) {
			logger.error("ERROR: Unexpected content - Message is null");
			return;
		}
	
		try {
			WorkMessageUtils.setWorkMessageState(msg, ctx, this);
			msgState.execute();
		} 
		
		catch (Exception e) {
			// TODO add logging
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}
		
		System.out.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}



	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		handleMessage(msg, ctx.channel(),ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		try{
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	
	public WorkMessageState getMsgState() {
		return msgState;
	}

	public void setMsgState(WorkMessageState msgState) {
		this.msgState = msgState;
	}
	
	public ServerState getState() {
		return state;
	}

	public void setState(ServerState state) {
		this.state = state;
	}

}