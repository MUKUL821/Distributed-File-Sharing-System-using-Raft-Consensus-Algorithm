package gash.router.dns.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DNSConnection {

	public void start() {

				
				
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
					
					b.childHandler(new DNSServerInit());
					
					ChannelFuture f = b.bind(4567).syncUninterruptibly();
					
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
		}
	
