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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.util.Base64;

import gash.router.container.GlobalRoutingConf;
import gash.router.container.RoutingConf;
import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.dbhandler.GuavaHandler;
import gash.router.server.dbhandler.MongoDBHandler;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.paralleltasks.ChunkTransferThread;
import gash.router.server.paralleltasks.HandleFileReadRequestsThread;
import gash.router.server.tasks.NoOpBalancer;
import gash.router.server.tasks.TaskList;
import gash.router.server.utils.WorkMessageUtils;
import global.Global.GlobalMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

public class MessageServer {
	protected static Logger logger = LoggerFactory.getLogger("server");

	protected static volatile HashMap<Integer, ServerBootstrap> bootstrap = new HashMap<Integer, ServerBootstrap>();

	private static volatile ConcurrentHashMap<String, Integer> fileChunkMap = new ConcurrentHashMap<String, Integer>();

	public static volatile ConcurrentHashMap<String, Integer> fileFollowerAckCountMap = new ConcurrentHashMap<String, Integer>();

	private static volatile ConcurrentHashMap<String, Integer> fileChunkUpdateMap = new ConcurrentHashMap<String, Integer>();
	// public static final String sPort = "port";
	// public static final String sPoolSize = "pool.size";

	protected RoutingConf conf;
	protected GlobalRoutingConf globalConf;
	protected boolean background = false;

	// private MongoDBHandler mongo;
	/**
	 * initialize the server with a configuration of it's resources
	 * 
	 * @param cfg
	 */

	public MessageServer(File cfg, File gcfg) {
		init(cfg);
		globalInit(gcfg);
	}

	public MessageServer(RoutingConf conf) {
		this.conf = conf;
	}

	public void release() {
	}

	public void startServer() {
		try {
			StartWorkCommunication comm = new StartWorkCommunication(conf);
			logger.info("Work starting");

			// We always start the worker in the background
			Thread cthread = new Thread(comm);
			cthread.start();
			HandleFileReadRequestsThread r = new HandleFileReadRequestsThread();
			Thread readThread = new Thread(r);
			readThread.start();

			WorkStealingThread wst = new WorkStealingThread();
			Thread wstThread = new Thread(wst);
			wstThread.start();

			StartCommandCommunication comm2 = new StartCommandCommunication(conf);
			logger.info("Command starting");
			// ServerState.state="Leader";

			StartGlobalCommunication s = new StartGlobalCommunication(globalConf);
			Thread globalCommunicationThread = new Thread(s);
			globalCommunicationThread.start();
			if (background) {
				Thread cthread2 = new Thread(comm2);
				cthread2.start();
			} else
				comm2.run();

		} catch (Exception e) {
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	/**
	 * static because we need to get a handle to the factory from the shutdown
	 * resource
	 */
	public static void shutdown() {
		try {
			logger.info("Server shutdown");
			System.exit(0);
		} catch (Exception e) {
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	private void init(File cfg) {
		try {
			if (!cfg.exists())
				throw new RuntimeException(cfg.getAbsolutePath() + " not found");
			// resource initialization - how message are processed
			BufferedInputStream br = null;
			try {
				byte[] raw = new byte[(int) cfg.length()];
				br = new BufferedInputStream(new FileInputStream(cfg));
				br.read(raw);
				conf = JsonUtil.decode(new String(raw), RoutingConf.class);
				if (!verifyConf(conf))
					throw new RuntimeException("verification of configuration failed");
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	private void globalInit(File gcfg) {
		try {
			if (!gcfg.exists())
				throw new RuntimeException(gcfg.getAbsolutePath() + " not found");
			// resource initialization - how message are processed
			BufferedInputStream br = null;
			try {
				byte[] raw = new byte[(int) gcfg.length()];
				br = new BufferedInputStream(new FileInputStream(gcfg));
				br.read(raw);
				globalConf = JsonUtil.decode(new String(raw), GlobalRoutingConf.class);
				if (!verifyConf(globalConf))
					throw new RuntimeException("verification of global configuration failed");
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception Occurred : " + e.getMessage());
		}
	}

	private boolean verifyConf(RoutingConf conf) {
		return (conf != null);
	}

	private boolean verifyConf(GlobalRoutingConf conf) {
		return (conf != null);
	}

	/**
	 * initialize netty communication
	 * 
	 * @param port
	 *            The port to listen to
	 */
	public static class StartCommandCommunication implements Runnable {

		RoutingConf conf;

		public StartCommandCommunication(RoutingConf conf) {
			this.conf = conf;
		}

		public void run() {
			// construct boss and worker threads (num threads = number of cores)
			try {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();

				try {
					ServerBootstrap b = new ServerBootstrap();
					bootstrap.put(conf.getCommandPort(), b);

					b.group(bossGroup, workerGroup);
					b.channel(NioServerSocketChannel.class);
					b.option(ChannelOption.SO_BACKLOG, 100);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);
					// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

					boolean compressComm = false;
					b.childHandler(new CommandInit(conf, compressComm));

					// Start the server.
					logger.info("Starting command server (" + conf.getNodeId() + "), listening on port = "
							+ conf.getCommandPort());
					ChannelFuture f = b.bind(conf.getCommandPort()).syncUninterruptibly();

					logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
							+ f.channel().isWritable() + ", act: " + f.channel().isActive());

					// block until the server socket is closed.
					f.channel().closeFuture().sync();

				} catch (Exception ex) {
					// on bind().sync()
					logger.error("Failed to setup handler.", ex);
				} finally {
					// Shut down all event loops to terminate all threads.
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}

			catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}
	}

	/**
	 * initialize netty communication
	 * 
	 * @param port
	 *            The port to listen to
	 */
	public static class StartWorkCommunication implements Runnable {
		ServerState state;
		// for atomic reference
		MongoDBHandler mongoHandler;
		GuavaHandler guavaHandler;
		private static AtomicReference<StartWorkCommunication> instance = new AtomicReference<StartWorkCommunication>();

		public static StartWorkCommunication getInstance() {
			return instance.get();
		}

		public StartWorkCommunication(RoutingConf conf) {

			try {
				if (conf == null)
					throw new RuntimeException("missing conf");

				state = new ServerState();
				state.setConf(conf);
				instance.set(this);
				TaskList tasks = new TaskList(new NoOpBalancer());
				state.setTasks(tasks);

				EdgeMonitor emon = new EdgeMonitor(state);
				Thread t = new Thread(emon);
				t.start();

			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		/* Return ArrayList of Chunks */
		public ArrayList<routing.Pipe.File> read(String fileName) throws Exception {

			try {
				ArrayList<routing.Pipe.File> fileChunks = null;
				mongoHandler = new MongoDBHandler();
				guavaHandler = new GuavaHandler();
			
				fileChunks = guavaHandler.retrieveData(fileName);
				
				if (fileChunks == null) {
					fileChunks = mongoHandler.retrieveData(fileName);
					guavaHandler.insertCompleteFile(fileChunks);
					
				}
				return fileChunks;
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
				return null;
			}
		}

		public boolean check(String fileName) {
			try {
				mongoHandler = new MongoDBHandler();
				return mongoHandler.fileCheck(fileName);
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
				return false;
			}
		}

		public void run() {
			// construct boss and worker threads (num threads = number of cores)
			try {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();

				try {
					ServerBootstrap b = new ServerBootstrap();
					bootstrap.put(state.getConf().getWorkPort(), b);

					b.group(bossGroup, workerGroup);
					b.channel(NioServerSocketChannel.class);
					b.option(ChannelOption.SO_BACKLOG, 100);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);
					// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

					boolean compressComm = false;
					b.childHandler(new WorkInit(state, compressComm));

					// Start the server.
					logger.info("Starting work server (" + state.getConf().getNodeId() + "), listening on port = "
							+ state.getConf().getWorkPort());
					ChannelFuture f = b.bind(state.getConf().getWorkPort()).syncUninterruptibly();

					logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
							+ f.channel().isWritable() + ", act: " + f.channel().isActive());

					// block until the server socket is closed.
					f.channel().closeFuture().sync();

				} catch (Exception ex) {
					// on bind().sync()
					logger.error("Failed to setup handler.", ex);
				} finally {
					// Shut down all event loops to terminate all threads.
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();

					// shutdown monitor
					EdgeMonitor emon = state.getEmon();
					if (emon != null)
						emon.shutdown();
				}
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void store(HashMap<Integer, Channel> channelMap, CommandMessage msg) throws IOException {

			try {
				FileOutputStream fileOuputStream = new FileOutputStream(msg.getRequest().getFileName());
				fileOuputStream.write(msg.getRequest().getFile().getData().toByteArray());
				fileOuputStream.close();
				// logger.info"chk ???");
				// state.setChannelMap(channelMap);
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void delete(String filename, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
			try {
				boolean successMongo = false;
				boolean successGuava = false;
				successMongo = mongo.deleteData(filename);
				successGuava = guava.deleteData(filename);

				if (successMongo && successGuava)
					logger.info("Deletion successfull");

			} catch (Exception e) {

			}

		}

		public void update(CommandMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
			try {
				boolean successMongo = false;
				routing.Pipe.File file = msg.getRequest().getFile();
				String filename = file.getFilename();
				int chunkID = file.getChunkId();
				int chunkCount = file.getChunkCount();
				// delete the all chunks of file present in db
				byte[] byteData = file.getData().toByteArray();
				// delete the all chunks of file present in db
				String data = Base64.encodeBytes(byteData);
				// successMongo =
				// mongo.updateData(filename,chunkID,chunkCount,data);
				successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					// logger.infofileChunkMap+","+fileChunkMap.containsKey(msg.getRequest().getFile().getFilename()));
					if (!fileChunkUpdateMap.containsKey(msg.getRequest().getFile().getFilename())) {
						mongo.deleteData(filename);
						fileChunkUpdateMap.put(msg.getRequest().getFile().getFilename(), 1);
					}

					upload(msg, mongo, guava, ctx);
				}
			} catch (Exception e) {

			}

		}

		public void update(WorkMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
			try {
				boolean successMongo = false;
				String filename = msg.getUpdateFile().getFileName();
				int chunkID = msg.getUpdateFile().getChunkId();
				int chunkCount = msg.getUpdateFile().getChunkCount();
				byte[] byteData = msg.getUpdateFile().getData().toByteArray();
				// delete the all chunks of file present in db
				String data = Base64.encodeBytes(byteData);

				successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					if (!fileChunkUpdateMap.containsKey(filename)) {
						// delete the all chunks of file present in db
						mongo.deleteData(filename);
						fileChunkUpdateMap.put(filename, 1);
					}

					upload(msg, mongo, guava, ctx);
				}
			} catch (Exception e) {

			}

		}

		public void upload(CommandMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
            try {
				boolean successMongo = false;
				routing.Pipe.File file = msg.getRequest().getFile();
				String filename = file.getFilename();
				int chunkID = file.getChunkId();
				int chunkCount = file.getChunkCount();
               byte[] byteData = file.getData().toByteArray();
				String data = Base64.encodeBytes(byteData);
			    successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					if (!fileChunkMap.containsKey(msg.getRequest().getFile().getFilename())) {
						fileChunkMap.put(msg.getRequest().getFile().getFilename(), 1);
					} else {
						int count = fileChunkMap.get(msg.getRequest().getFile().getFilename());
						fileChunkMap.put(msg.getRequest().getFile().getFilename(), count + 1);
					}
                   // if the node is a leader, it replicates it to other nodes
					if (ServerState.state.equals("Leader")) 
					{

						for (EdgeInfo ei : EdgeList.activeEdges.values()) {
							ChunkTransferThread ct = new ChunkTransferThread(msg, null, ei, state);
							Thread cThread = new Thread(ct);
							cThread.start();
						}
						
						if (fileChunkMap.get(msg.getRequest().getFile().getFilename()) >= msg.getRequest().getFile()
								.getChunkCount()) {

							Thread.sleep(5000);
							if (fileFollowerAckCountMap.containsKey(msg.getRequest().getFile().getFilename())) {
								long count = EdgeList.map.mappingCount() / 2;
								if (fileFollowerAckCountMap.get(msg.getRequest().getFile().getFilename()) >= (count)) {
									WorkMessage wm = WorkMessageUtils
											.createFileTransferAck(msg.getRequest().getFile().getFilename(), state);
									ctx.channel().writeAndFlush(wm);
								}
							}
						}

					} else {
						ctx.channel().writeAndFlush(WorkMessageUtils.createChunkAck(msg, state));

						if (fileChunkMap.get(msg.getRequest().getFile().getFilename()) == msg.getRequest().getFile()
								.getChunkCount()) {

							ctx.channel().writeAndFlush(WorkMessageUtils
									.createFileTransferAckFollower(msg.getRequest().getFile().getFilename(), state));
						}
					}
               } else {
					logger.info("failed to upload inb db..");
				}
			
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}

		}

		public void upload(WorkMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {

			try {
				boolean successMongo = false;
				String filename = msg.getUpdateFile().getFileName();
				int chunkID = msg.getUpdateFile().getChunkId();
				int chunkCount = msg.getUpdateFile().getChunkCount();
				byte[] byteData = msg.getUpdateFile().getData().toByteArray();
				// delete the all chunks of file present in db
				String data = Base64.encodeBytes(byteData);

				successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					if (!fileChunkMap.containsKey(filename)) {
						fileChunkMap.put(filename, 1);
					} else {
						int count = fileChunkMap.get(filename);
						fileChunkMap.put(filename, count + 1);
					}
				} else {
					logger.info("failed to upload inb db..");
				}

			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}

		}
	}

	public static class StartGlobalCommunication implements Runnable {
		ServerState gState;
		// for atomic reference
		MongoDBHandler mongoHandler;
		GuavaHandler guavaHandler;
		private static AtomicReference<StartGlobalCommunication> instance = new AtomicReference<StartGlobalCommunication>();

		public static StartGlobalCommunication getInstance() {
			return instance.get();
		}

		public StartGlobalCommunication(GlobalRoutingConf conf) {
			try {
				if (conf == null)
					throw new RuntimeException("missing conf");

				gState = new ServerState();
				gState.setgConf(conf);
				instance.set(this);
				TaskList tasks = new TaskList(new NoOpBalancer());
				gState.setTasks(tasks);

				GlobalEdgeMonitor gEmon = new GlobalEdgeMonitor(gState);
				Thread t = new Thread(gEmon);
				t.start();
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void run() {
			// construct boss and worker threads (num threads = number of cores)
			try {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();

				try {
					ServerBootstrap b = new ServerBootstrap();
					bootstrap.put(gState.getgConf().getGlobalPort(), b);

					b.group(bossGroup, workerGroup);
					b.channel(NioServerSocketChannel.class);
					b.option(ChannelOption.SO_BACKLOG, 100);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);
					// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

					boolean compressComm = false;
					b.childHandler(new GlobalInit(gState, compressComm));

					// Start the server.
					logger.info("Starting global server (" + gState.getgConf().getClusterId()
							+ "), listening on port = " + gState.getgConf().getGlobalPort());
					ChannelFuture f = b.bind(gState.getgConf().getGlobalPort()).syncUninterruptibly();

					logger.info(f.channel().localAddress() + " -> open: " + f.channel().isOpen() + ", write: "
							+ f.channel().isWritable() + ", act: " + f.channel().isActive());

					f.channel().closeFuture().sync();
				} catch (Exception ex) {
					logger.error("Failed to setup handler.", ex);
				} finally {
					// Shut down all event loops to terminate all threads.
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void store(HashMap<Integer, Channel> channelMap, CommandMessage msg) throws IOException {

			try {
				FileOutputStream fileOuputStream = new FileOutputStream(msg.getRequest().getFileName());
				fileOuputStream.write(msg.getRequest().getFile().getData().toByteArray());
				fileOuputStream.close();
				// logger.info"chk ???");
				// state.setChannelMap(channelMap);
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void upload(GlobalMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
			try {
				boolean successMongo = false;
				String filename = msg.getRequest().getFile().getFilename();
				int chunkID = msg.getRequest().getFile().getChunkId();
				int chunkCount = msg.getRequest().getFile().getTotalNoOfChunks();
				byte[] byteData = msg.getRequest().getFile().getData().toByteArray();
				// delete the all chunks of file present in db
				String data = Base64.encodeBytes(byteData);

				// Mongo DB insertion (While Upload Cache is not used) -- Cache
				// is used in Read part
				successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					// logger.infofileChunkMap+","+fileChunkMap.containsKey(msg.getRequest().getFile().getFilename()));
					if (!fileChunkMap.containsKey(msg.getRequest().getFile().getFilename())) {
						fileChunkMap.put(msg.getRequest().getFile().getFilename(), 1);
					} else {
						int count = fileChunkMap.get(msg.getRequest().getFile().getFilename());
						fileChunkMap.put(msg.getRequest().getFile().getFilename(), count + 1);
					}

					// if the node is a leader, it replicates it to other nodes
					if (ServerState.state.equals("Leader")) {
						System.out.println("Leader,replicating to other nodes");
						for (EdgeInfo ei : EdgeList.activeEdges.values()) {

							// ChunkTransferThread ct = new
							// ChunkTransferThread(null,msg,ei,StartWorkCommunication.getInstance().state);
							// Thread cThread =new Thread(ct);
							// cThread.start();
						}

					}

					logger.info("database storage successfull");

				} else {
					logger.info("failed to upload inb db..");
				}
				if (ServerState.state.equals("Leader")) {
					try {
						// logger.info("filecount from map : " +
						// fileChunkMap.get(msg.getRequest().getFile().getFilename()));
						if (fileChunkMap.get(filename) >= chunkCount) {
							// logger.info"inside first if");
							// logger.info"before sleep");
							Thread.sleep(5000);
							if (fileFollowerAckCountMap.containsKey(msg.getRequest().getFile().getFilename())) {
								// logger.info"Inside final leader check if
								// "+EdgeList.map.mappingCount()/2+","+fileFollowerAckCountMap.get(msg.getRequest().getFile().getFilename()));
								long count = EdgeList.map.mappingCount() / 2;
								if (fileFollowerAckCountMap.get(msg.getRequest().getFile().getFilename()) >= (count)) {
									logger.info("SENDING FILE TO CLIENT FROM LEADER.........................");
									WorkMessage wm = WorkMessageUtils.createFileTransferAck(
											msg.getRequest().getFile().getFilename(),
											StartWorkCommunication.getInstance().state);
									ctx.channel().writeAndFlush(wm);
									// ctx.channel().writeAndFlush(msg);
								}
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			} catch (Exception e) {
				logger.error("Exception Occurred : " + e.getMessage());
			}
		}

		public void update(GlobalMessage msg, MongoDBHandler mongo, GuavaHandler guava, ChannelHandlerContext ctx) {
			try {
				boolean successMongo = false;
				String filename = msg.getRequest().getFile().getFilename();
				int chunkID = msg.getRequest().getFile().getChunkId();
				int chunkCount = msg.getRequest().getFile().getTotalNoOfChunks();
				String data = msg.getRequest().getFile().getData().toString();
				// delete the all chunks of file present in db

				// successMongo =
				// mongo.updateData(filename,chunkID,chunkCount,data);
				successMongo = mongo.insertData(filename, chunkID, chunkCount, data);
				if (successMongo == true) {
					// logger.infofileChunkMap+","+fileChunkMap.containsKey(msg.getRequest().getFile().getFilename()));
					if (!fileChunkUpdateMap.containsKey(msg.getRequest().getFile().getFilename())) {
						mongo.deleteData(filename);
						fileChunkUpdateMap.put(msg.getRequest().getFile().getFilename(), 1);
					}
					upload(msg, mongo, guava, ctx);
				}
			} catch (Exception e) {

			}

		}

	}

	/**
	 * help with processing the configuration information
	 * 
	 * @author gash
	 *
	 */
	public static class JsonUtil {
		private static JsonUtil instance;

		public static void init(File cfg) {

		}

		public static JsonUtil getInstance() {
			if (instance == null)
				throw new RuntimeException("Server has not been initialized");

			return instance;
		}

		public static String encode(Object data) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(data);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				return null;
			}
		}

		public static <T> T decode(String data, Class<T> theClass) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(data.getBytes(), theClass);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				return null;
			}
		}
	}
}
