package gash.router.server.utils;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.FileRequestContainer;
import gash.router.server.FileRequestObject;
import gash.router.server.paralleltasks.DBRequestFollowersThread;


public class WorkStealingUtil {
	private static  Logger logger = LoggerFactory.getLogger("WorkStealingUtil");
	public boolean isBusy(){
		//Check if the queue has more than 3 values
		if(FileRequestContainer.readRequestQueue.size()>2){
			return true;
		}
		return false;
	}
	
	public  static void workStealingActionHandler(int nodeId){
		try{
		if(FileRequestContainer.readRequestQueue.size()>=1)
		{
			try {
				logger.info("Transfering work to Node:"+nodeId+" ... Queue Size:"+FileRequestContainer.readRequestQueue.size());
				FileRequestObject fro = FileRequestContainer.readRequestQueue.take();
				//Channel ch= fro.getCtx().channel();
				String fileName = fro.getFileName();
				boolean success = false;
				String requestId = UUID.randomUUID().toString();
				success = DBRequestFollowersThread.sendWorkSharingRequest(requestId,fro.getEi(),fileName,fro.getCtx());			
				Thread.sleep(1000);
				if(!DBRequestFollowersThread.fileRequestClientMap.get(requestId).isResult()) {
					FileRequestObject fro1 = new FileRequestObject(fro.getCtx(), fileName, "command");
					FileRequestContainer.readRequestQueue.add(fro1);
				} 
			} catch (Exception e) {
				logger.error(e.getMessage());
			}			
		} else {
			logger.info("Not transfering work to Node:"+nodeId+" ... I am not busy");
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
}
