package gash.project.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import gash.router.client.CommConnection;
import gash.router.client.CommListener;

public class ClientFunc {

	private String host;
	private int port;
	private CommConnection comm;
	
	protected static Logger logger = LoggerFactory.getLogger("client");
	public ClientFunc(String host, int port) {
		this.host = host;
		this.port = port;
		init();
	}

	public void init() {
		comm = new CommConnection(host,port);
	}
	
	public void addListener(CommListener listener){
		
		comm.addListener(listener);
	}

public void sendImage(String requestID, ByteString bs, String fname, String reqtype) throws Exception {
		
		System.out.println("ByteString size : " + bs.size());
		byte[] myByteImage = bs.toByteArray();
	    routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
		routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
		routing.Pipe.Request.Builder req= routing.Pipe.Request.newBuilder();
		
        final int CHUNKSIZE = 1048576 ; // 1 Megabytes file chunks
		
		 int size = myByteImage.length;
		 System.out.println(size);
		 if(size>CHUNKSIZE){
		 int numberofchunks = (int) (size/CHUNKSIZE);
		 System.out.println(numberofchunks);
		 System.out.println("Number of Chunks : "+ numberofchunks);
		 System.out.println("Total Size :" + size);
			for(int i=0;i<=numberofchunks-1;i++)
			{	
				ByteString ch= null;
				if(i==numberofchunks-1)
				{
					System.out.println("Start Chunk Size : " + i*CHUNKSIZE + " End Chunk Size :" + CHUNKSIZE);
					 ch= ByteString.copyFrom(myByteImage, i*CHUNKSIZE, size-i*CHUNKSIZE);
				}
				else
				{
					System.out.println("Start Chunk Size : " + i*CHUNKSIZE + " End Chunk Size :" + CHUNKSIZE);
					ch = ByteString.copyFrom(myByteImage, i*CHUNKSIZE, CHUNKSIZE);
				}
		 	
			hb.setClusterId(999);
		 	hb.setTime(System.currentTimeMillis());
		
			req.setRequestId(requestID);
			if(reqtype.equalsIgnoreCase("write"))
			req.setRequestType(routing.Pipe.RequestType.WRITE);
			if(reqtype.equalsIgnoreCase("update"))
				req.setRequestType(routing.Pipe.RequestType.UPDATE);
			routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
		    file.setFilename(fname);
		    file.setData(ch);
		    file.setChunkId(i);
		    file.setChunkCount(numberofchunks);
		  
		    req.setFile(file.build());
		    
			rb.setGlobalHeader(hb.build());
			rb.setRequest(req.build());
			comm.enqueue(rb.build());
			}
		 }
			else{
				hb.setClusterId(999);
			 	hb.setTime(System.currentTimeMillis());
			    req.setRequestId(requestID);
			    if(reqtype.equalsIgnoreCase("write"))
				req.setRequestType(routing.Pipe.RequestType.WRITE);
				if(reqtype.equalsIgnoreCase("update"))
				req.setRequestType(routing.Pipe.RequestType.UPDATE);
				routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
			    file.setFilename(fname);
			    file.setChunkCount(1);
			    file.setChunkId(1);
			    file.setData(bs);
			    req.setFile(file.build());
			    
				rb.setGlobalHeader(hb.build());
				rb.setRequest(req.build());
				comm.enqueue(rb.build());
			}
		
	
	}


	public void readImage(String fname){
        routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
		routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
	 	
		hb.setClusterId(999);
	 	hb.setTime(System.currentTimeMillis());
	
	    routing.Pipe.Request.Builder req= routing.Pipe.Request.newBuilder();
		req.setRequestId("67");
		req.setRequestType(routing.Pipe.RequestType.READ);
		routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
	    file.setFilename(fname);
	    req.setFile(file.build());
	    
		rb.setGlobalHeader(hb.build());
		rb.setRequest(req.build());
	
	  //  rb.setPing(false);
	    //rb.setReqId(requestID);
     //    poke.comm.Image.Request req = r.build();	  

		try {			
			comm.enqueue(rb.build());
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}


	public void upload(String fname, int id, String path) throws Exception {
		try {
//HERE WE HAVE TO INCLUDE CODE FOR CHUNKS
			byte[] myByteImage;		
		
			//fileChunk(fname);
			
			System.out.println(path+fname);
			/*change-here*/
			File file = new File(path+fname);
			FileInputStream fis = new FileInputStream(file);
			
			myByteImage = IOUtils.toByteArray(fis);
			String uniqueRequestID = UUID.randomUUID().toString().replaceAll("-", "");		
			System.out.println(myByteImage.length);
			ByteString bs = ByteString.copyFrom(myByteImage);		
			sendImage(uniqueRequestID, bs,fname,"write");
			
		} catch (IOException e) {
			e.printStackTrace();		
		}
	}
	

	
	public void updateImage(String fname,String path) throws Exception {
		//HERE WE HAVE TO INCLUDE CODE FOR CHUNKS
		try{
			byte[] myByteImage;		
			
			//fileChunk(fname);
			
			System.out.println(path+fname);
			/*change-here*/
			File file = new File(path+fname);
			FileInputStream fis = new FileInputStream(file);
			
			myByteImage = IOUtils.toByteArray(fis);
		
		String uniqueRequestID = UUID.randomUUID().toString().replaceAll("-", "");		
		System.out.println(myByteImage.length);
		ByteString bs = ByteString.copyFrom(myByteImage);
		
		
		
		sendImage(uniqueRequestID, bs,fname,"update");
		
	} catch (IOException e) {

		e.printStackTrace();
	
	
}
	}

	public void deleteImage(String fname) {  routing.Pipe.CommandMessage.Builder rb = routing.Pipe.CommandMessage.newBuilder();
	routing.Pipe.GlobalHeader.Builder hb = routing.Pipe.GlobalHeader.newBuilder();
 	
	hb.setClusterId(999);
 	hb.setTime(System.currentTimeMillis());

    routing.Pipe.Request.Builder req= routing.Pipe.Request.newBuilder();
	req.setRequestId("697");
	req.setRequestType(routing.Pipe.RequestType.DELETE);
	routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
    file.setFilename(fname);
    req.setFile(file.build());
    
	rb.setGlobalHeader(hb.build());
	rb.setRequest(req.build());
try {			
		comm.enqueue(rb.build());
	} catch (Exception e) {
		logger.warn("Unable to deliver message, queuing");
	}// TODO Auto-generated method stub
		
	}
	
	
	
	
	
}
