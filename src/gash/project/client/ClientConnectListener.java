package gash.project.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.client.AckHandler;
import gash.router.client.CommListener;
import gash.router.server.FileComparator;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

public class ClientConnectListener implements CommListener {

	protected static Logger logger = LoggerFactory.getLogger("connect");
	static HashMap<String, ArrayList<routing.Pipe.File>> fileUploadInProgess;
	private String id;
	int i = 0;
	static
	{
	fileUploadInProgess = new HashMap<>();
	}
	public ClientConnectListener(String id) {
		this.id = id;
	}
	@Override
	public String getListenerID() {
		
		return id;
	}

	@Override
	public void onMessage(Object msg) {
		System.out.println(Client.choice+" operation happening");
		if(Client.choice.equalsIgnoreCase("r"))
		{
			CommandMessage ms = (CommandMessage)msg;
			System.out.println(((CommandMessage) msg).getRequest().getFile().getFilename());
			i++;
			//RESPONSE FROM SERVER
			String imgname = ms.getResponse().getFile().getFilename();//  string for time being
			//String imgName = msg.getPayload().getReqId()+".png";
			
			//receive the chunks
			recieveChunks(ms);
		System.out.println("Received message from server!!");
		}
		if(msg instanceof WorkMessage||Client.choice.equalsIgnoreCase("u")){
			
			System.out.println("acknowledment received..");
			CommandMessage ms = (CommandMessage)msg;
			//if(ms.hasFileUploadedAck())
			//{
				System.out.println("acknowledment received..");	
				//System.out.println("FileName" +ms.getRequest().getFileName());
				System.out.println("FileName" +ms.getRequest().getFile().getFilename());
				AckHandler.ackMap.put(ms.getRequest().getFile().getFilename(), true);
			//}
		}

		
	}
	private void recieveChunks(CommandMessage msg) {
		System.out.println("Inside receive chunks");
		if (msg == null) {
			// TODO add logging
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}
		else if(msg.hasMessage())
			System.out.println("Message from server"+msg.getMessage());
	    else if(msg.hasPing())
	    	System.out.println("Message from server"+msg.getPing());
	    else if(msg.hasResponse())
	    {
	    	System.out.println("Inside hasResponse");
	    	routing.Pipe.File currFile =  msg.getResponse().getFile();
			boolean completedFlag = false;
			byte[] byteImage = null;

			
			if(fileUploadInProgess.containsKey(currFile.getFilename()))
			{
				
				fileUploadInProgess.get(currFile.getFilename()).add(currFile);
			}
			else
			{
				
				ArrayList<routing.Pipe.File> set = new ArrayList<>();
				set.add(currFile);
				fileUploadInProgess.put(currFile.getFilename(), set);
			}
				
			if(fileUploadInProgess.get(currFile.getFilename()).size() == currFile.getChunkCount())
			{
				completedFlag = true;
				
				System.out.println("Starting to merge");
				mergeFilesAndWriteDisk(currFile);
				fileUploadInProgess.remove(currFile.getFilename());
				//writeInDisk(currFile.getFilename(),byteImage);
			}
	    	
	    }
		}
	/*
	private void writeInDisk(String filename, byte[] byteImage) {
		 InputStream in = new ByteArrayInputStream(byteImage);
	        BufferedImage bImageFromConvert;
			try {
				
				bImageFromConvert = ImageIO.read(in);
				ImageIO.write(bImageFromConvert, "jpg", new File(
				        ""+filename));
				        
				System.out.println("Image Output --------------------------");
				System.out.println(byteImage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	*/
	private void mergeFilesAndWriteDisk(routing.Pipe.File currFile) {
		// TODO Auto-generated method stub
		System.out.println("Merging Files");
		ArrayList<routing.Pipe.File> fileChunks = fileUploadInProgess.get(currFile.getFilename());
		ArrayList<routing.Pipe.File> set=fileChunks;
		java.util.Collections.sort(fileChunks, new FileComparator());
		int length = 0;
		for(routing.Pipe.File chunk : fileChunks)
			length+=chunk.getData().toByteArray().length;
		byte mergedfile[]= new byte[length];
		length=0;
		String fileName = null;
		for(routing.Pipe.File chunk : fileChunks)
		{
			//System.out.println(chunk.getData().toByteArray().length);
			System.arraycopy(chunk.getData().toByteArray(), 0, mergedfile,length , chunk.getData().toByteArray().length);
			length+=chunk.getData().toByteArray().length;
			fileName = chunk.getFilename();
		}
		File file = new File("/home/bala/bitbucket/output/"+ fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
				fos.write(mergedfile);
				System.out.println("Writing merged File");
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//return fileByte;
		/*
		System.out.println("Printing file Byte Array -------------------------------------");
		for(routing.Pipe.File chunk : fileChunks)
		{
			System.out.println(Arrays.toString(chunk.getData().toByteArray()));
		}
		*/
	}
}


