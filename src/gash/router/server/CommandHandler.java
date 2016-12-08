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


import java.util.ArrayList;
import java.util.HashMap;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import gash.router.container.RoutingConf;

import gash.router.server.messagestates.command.CommandMessageState;
import gash.router.server.utils.CommandMessageUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;

import pipe.common.Common.Failure;
import routing.Pipe.CommandMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class CommandHandler extends SimpleChannelInboundHandler<CommandMessage> {
	protected static Logger logger = LoggerFactory.getLogger("cmd");
	//private static HashMap<Integer, Channel> channelMap=new HashMap<Integer, Channel>();
	protected RoutingConf conf;
	
	protected CommandMessageState msgState;
	static HashMap<String, ArrayList<routing.Pipe.File>> fileUploadInProgess;
	public CommandHandler(RoutingConf conf) {
		if (conf != null) {
			this.conf = conf;
		}
	}
	static
	{
		fileUploadInProgess = new HashMap<>();
	}
	/**
	 * override this method to provide processing behavior. This implementation
	 * mimics the routing we see in annotating classes to support a RESTful-like
	 * behavior (e.g., jax-rs).
	 * 
	 * @param msg
	 */
	public void handleMessage(ChannelHandlerContext ctx, CommandMessage msg, Channel channel) {
	try{
		try{
		if (msg == null) {
			logger.error("ERROR: Unexpected content - " + msg);
			return;
		}
		
		CommandMessageUtils.setMessageState(msg, ctx, this);
		msgState.execute();	

		} catch (Exception e) {
			// TODO add logging
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(conf.getNodeId());
			eb.setRefId(msg.getGlobalHeader().getClusterId());
			eb.setMessage(e.getMessage());
			CommandMessage.Builder rb = CommandMessage.newBuilder(msg);
			rb.setFailure(eb.build());
			channel.write(rb.build());
		}
		
		System.out.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	
/*
	private byte[] mergeFiles(routing.Pipe.File currFile) {
		// TODO Auto-generated method stub
		ArrayList<routing.Pipe.File> fileChunks = fileUploadInProgess.get(currFile.getFilename());
		java.util.Collections.sort(fileChunks, new FileComparator());
		int length = 0;
		for(routing.Pipe.File chunk : fileChunks)
			length+=chunk.getData().size();
		byte mergedfile[]= new byte[length];
		length=0;
	
		for(routing.Pipe.File chunk : fileChunks)
		{
			System.arraycopy(chunk.getData().toByteArray(), 0, mergedfile,length , chunk.getData().size());
			length+=chunk.getData().size();
		}
		return mergedfile;
	}
*/
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
	protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
		handleMessage(ctx, msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
		try{
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}
	public CommandMessageState getMsgState() {
		return msgState;
	}

	public void setMsgState(CommandMessageState msgState) {
		this.msgState = msgState;
	}

}