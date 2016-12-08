package gash.router.dns.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.project.client.DNSInit;
import gash.router.server.CommandInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DNSMappingServer {

	protected static Logger logger = LoggerFactory.getLogger("DNSserver");

	private DNSConnection dnsConn;
	public DNSMappingServer(){
		dnsConn = new DNSConnection();
		dnsConn.connection();
		
	
	}
	public class DNSConnection {/*
	public void connection() {

		
		
		// TODO Auto-generated method stub
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.option(ChannelOption.SO_BACKLOG, 100);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			
			b.childHandler(new DNSInit());
		//	b.handler(new DNSServerInit());
			
			ChannelFuture f = b.bind(4569).syncUninterruptibly();
			
			// block until the server socket is closed.
			f.channel().closeFuture().sync();
		} catch (Exception ex) {

		}
		finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();


		}
	}
	*/
	public void connection(){
		try{
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap b = new ServerBootstrap();

				b.group(bossGroup, workerGroup);
				b.channel(NioServerSocketChannel.class);
				b.option(ChannelOption.SO_BACKLOG, 100);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);
				// b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR);

				boolean compressComm = false;
				b.childHandler(new DNSServerInit());

				// Start the server.
				logger.info("Starting DNS server, listening on port = "
						+ 4569);
				ChannelFuture f = b.bind(4569).syncUninterruptibly();

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
		
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	}
	
	}
