                        package gash.router.server.dbhandler;

import java.util.ArrayList;

import java.util.concurrent.ExecutionException;

import routing.Pipe.File;

public interface DbHandler {

	
	//public Map<String, String> removeData(String key);
	
	public void closeConnection();

	public ArrayList<File> retrieveData(String key) throws ExecutionException;
	
	boolean insertData(String imgname, int chunkId, int chunkCount, String imageFile);

	boolean deleteData(String filename);

	boolean updateData(String filename, int chunkID, int chunkCount, String data);
		
	}