package gash.router.server.dbhandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;

import routing.Pipe.File;


public class GuavaHandler implements DbHandler {
	private static Logger logger = LoggerFactory.getLogger("GuavaHandler");
	LoadingCache<String,routing.Pipe.File> fileCache;
	private static volatile GuavaHandler guavaCache;
	static
	{
		guavaCache = new GuavaHandler();
	}
	public GuavaHandler(){
		init();
	}
	
	public void init(){
		//create a cache for employees based on their file
		try{
	    fileCache = 
	       CacheBuilder.newBuilder()
	          .maximumSize(50) 
	          .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
	          .build(new CacheLoader<String, routing.Pipe.File>(){ // build the cacheloader
	          
	             @Override
	             public routing.Pipe.File load(String filename) throws Exception {
	                //make the expensive call
	               logger.info("not in cache.."); 
					return null ;
	             }
	          });
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	public void insertCompleteFile( ArrayList<routing.Pipe.File> fileChunks)
	{
		try{
		if(fileChunks == null || fileChunks.size() == 0)
			return;
		for(File chunk : fileChunks)
		{
			insertData(chunk.getFilename(), chunk.getChunkId(), chunk.getChunkCount(), chunk.getData().toString());
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}
	
	
	@Override
	public ArrayList<routing.Pipe.File> retrieveData(String key) throws ExecutionException {
		// TO GET THE DATA
			try{
				logger.info("GuavaCache " + guavaCache);
				ArrayList<routing.Pipe.File> file = guavaCache.getData(key);
				logger.info("File from Guava : " + file);
				if(file==null)return null;
				
				if(file.size()>0){
					for(routing.Pipe.File f : file){
						logger.info(" data : "+ f.getChunkId());
					}
				}
				return file;
			}
			catch(Exception e)
			{
				logger.error("Exception Occurred : " + e.getMessage() );
				return null;
			}
	}

	private ArrayList<routing.Pipe.File> getData(String string)  {
		try{
		boolean val =true;
		int start = 0;
		ArrayList<routing.Pipe.File> result = new ArrayList<routing.Pipe.File>();
		while(val){
			
			routing.Pipe.File eachfile = fileCache.getIfPresent(string+start);
			
	     	if(	eachfile!=null)
	     		result.add(eachfile);
	        else
	        	val=false;	
	     	start++;
		}
		logger.info("Guava Retrievaldata : " +result + " +size: " + result.size());
		if(result.size()>0)
		return result;
		else
			return null;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
		
	}
	@Override
	public void closeConnection() {
		
		
	}

	@Override
	public boolean insertData(String imgname, int chunkId, int chunkCount, String imageFile) {
		try{
		//based on file name
		//Multimap<String, routing.Pipe.File> filemap = ArrayListMultimap.create();
		routing.Pipe.File.Builder file= routing.Pipe.File.newBuilder();
		file.setFilename(imgname);
		file.setChunkCount(chunkCount);
		file.setChunkId(chunkId);
		  ByteString val = ByteString.copyFrom(imageFile.getBytes(StandardCharsets.UTF_8));
		    file.setData(val);
		
	    fileCache.put(imgname+chunkId,file.build());
		return true;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return false;
		}
	}
	@Override
	public boolean updateData(String filename, int chunkID, int chunkCount, String data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteData(String filename) {
		boolean val = true;
		int start =0;
	while(val){
			
			routing.Pipe.File eachfile = fileCache.getIfPresent(filename+start);
			
	     	if(	eachfile!=null)
	     	fileCache.invalidate(filename+start);
	        else
	        	val=false;	
	     	start++;
		}
	
	return true;
	}
	}