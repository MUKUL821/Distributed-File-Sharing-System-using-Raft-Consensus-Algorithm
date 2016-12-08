package gash.project.client;


import gash.router.client.AckHandler;
import gash.router.client.CommListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;
import pipe.work.Work.DNSRequest;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

import java.util.ArrayList;
import java.util.Scanner;

public class Client {

	private int id;
	static volatile String leaderHost= "" ;
	static volatile int leaderPort =-1;
	static ArrayList<String> host = new ArrayList<>();
	
	public Client(int id) {
	this.id =id;
	}

	public Client() {
		// TODO Auto-generated constructor stub
	}
	public static String choice="upload";
	
	public static void main(String[] args) throws Exception {
		 Scanner sc= new Scanner(System.in);  
		while(true)
		{
			System.out.println("Getting Leader info from DNS server");
			while(leaderHost.length()==0)
			{
				populateLeaderHostDetails();
				System.out.println(leaderHost.length());
				Thread.sleep(1000);
			}
			Thread.sleep(2000); 	
			System.out.println("Leader Host" + leaderHost  + " Leader Port "+leaderPort);
			 ClientFunc con = new ClientFunc(leaderHost, leaderPort);// give the cmdport here
				CommListener listener = new ClientConnectListener("My First Client");
				con.addListener(listener);
			    Client cl = new Client();
	
				 System.out.println("enter ur choice: r for read or u for upload ");
				 System.out.println("enter ur choice: d for delete or update for update ");
			  choice = sc.nextLine();
			 /*
			 choice = "u";
			 choice = "r";
			 */
				String path="/home/bala/bitbucket/";
			  //String path = "D:\\CMPE275\\project1\\";
				String fname = null;
			 if(choice.equalsIgnoreCase("u"))
			 {
				 System.out.println("enter filename");
					 fname = sc.next();
				//String fname = "test1.jpg";
						
				boolean ackReceived = false;
				//while(!ackReceived)
				//{
						System.out.println("Sending Image " );
						
						UploadThread ut = new UploadThread(cl,con, fname, path);
						Thread uThread = new Thread(ut);
						uThread.start();
						Thread.sleep(30000);
						System.out.println(AckHandler.ackMap);
						System.out.println(fname);
						ackReceived = AckHandler.ackMap.get(fname)==null?false:true;
					   // we are running asynchronously
						//System.out.println("\n REtrying in 30seconds");
				//}
				AckHandler.ackMap.remove(fname);
			  }
			 else
				 if(choice.equalsIgnoreCase("r")){
					//CHANGE IT..MAKE IT DYNAMIC
					 System.out.println("enter filename");
					 fname = sc.next();
					 cl.read(con,fname);
					 
				 }
			 else 
				 if(choice.equalsIgnoreCase("d")){
					 System.out.println("enter filename");
					 fname = sc.next();
					 cl.delete(con,fname);
				 }
			 else
				 if(choice.equalsIgnoreCase("update"))
				 {
					 System.out.println("enter filename");
					 fname = sc.next();
						boolean ackReceived = false;
						//while(!ackReceived)
						//{
								System.out.println("Sending Image for update " );
								
								UpdateThread ut = new UpdateThread(cl,con, fname, path);
								Thread uThread = new Thread(ut);
								uThread.start();
							//	Thread.sleep(30000);
								System.out.println(AckHandler.ackMap);
								System.out.println(fname);
								ackReceived = AckHandler.ackMap.get(fname)==null?false:true;
							   // we are running asynchronously
							//	System.out.println("\n REtrying in 30seconds");
						//}
						AckHandler.ackMap.remove(fname);			 }
			 
		}

	}

	private static void populateLeaderHostDetails() {
		// TODO Auto-generated method stub
		 String dnsHost = "169.254.203.42";
		 int dnsPort = 4569;

	        EventLoopGroup workerGroup = new NioEventLoopGroup();

	        try {
	            Bootstrap b = new Bootstrap(); // (1)
	            b.group(workerGroup); // (2)
	            b.channel(NioSocketChannel.class); // (3)
	            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
	            b.handler(new DNSInit());

	            // Start the client.
	            ChannelFuture f = b.connect(dnsHost, dnsPort).sync(); // (5)
	        	WorkState.Builder sb = WorkState.newBuilder();
				sb.setEnqueued(-1);
				sb.setProcessed(-1);

				DNSRequest.Builder bb = DNSRequest.newBuilder();
				bb.setPing(true);
				
				Header.Builder hb = Header.newBuilder();
				hb.setNodeId(1);
				hb.setDestination(2);
				hb.setTime(System.currentTimeMillis());
				

				WorkMessage.Builder wb = WorkMessage.newBuilder();
				wb.setHeader(hb);
				wb.setDnsRequest(bb);
				wb.setSecret(100);
	            // Wait until the connection is closed.
				f.channel().writeAndFlush(wb.build());
	            f.channel().closeFuture().sync();
	            
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            workerGroup.shutdownGracefully();
	        }
	}

	void upload(ClientFunc con, String fname, String path) throws Exception {
	
		con.upload(fname,id,path);	
	}

    private void read(ClientFunc con, String fname){
    	
    	con.readImage(fname);
    }
    void update(ClientFunc con, String fname, String path) throws Exception {
		
		con.updateImage(fname, path);
	}

	private void delete(ClientFunc con, String fname) {
		con.deleteImage(fname);
	}
    

}
