package gash.router.server.dbhandler;



import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.Base64;

public class MongoDBHandler implements DbHandler{
	
	public DBCollection dbCollection;
	protected static Logger logger = LoggerFactory.getLogger("MongoDB Handler");
	
	public MongoDBHandler() throws UnknownHostException {
		Mongo mongo = new Mongo("127.0.0.1", 27017);
		DB db = mongo.getDB("filedb");
		this.dbCollection = db.getCollection("fileCollection");
		
	}
	@Override
	public boolean updateData(String filename, int chunkID, int chunkCount, String data) {
  
       insertData(filename,chunkID,chunkCount,data);
    	
		return true;
	}


	@Override
	public boolean deleteData(String filename) {
		DBObject doc=new BasicDBObject();
	    doc.put("Filename", filename);
		DBCursor cursor = dbCollection.find(doc);
		while (cursor.hasNext()) {
			dbCollection.remove(cursor.next());
		}
		return true;
	
	}

	@Override
	public boolean insertData(String fname,int chunkId, int chunkCount,String data) {
		if(!duplicateCheck(fname,chunkId)){
		try {
			DBObject doc=new BasicDBObject();	
			doc.put("Filename", fname);
			doc.put("ChunkID", chunkId);
			doc.put("ChunkCount", chunkCount);
			doc.put("FileData", data );
			dbCollection.insert(doc);
			return true;
		} catch (Exception e) {
			logger.error("Cannot insert data into Mongo");
			return false;
		}}
		return true;
	}

	@Override
	public ArrayList<routing.Pipe.File> retrieveData(String fname) {
		try{
		ArrayList<routing.Pipe.File> data = new ArrayList<routing.Pipe.File>();
		
		routing.Pipe.File.Builder file = routing.Pipe.File.newBuilder();
		DBObject doc=new BasicDBObject();
	    doc.put("Filename", fname);
		
		DBCursor cursor=dbCollection.find(doc);
		
	
		while (cursor.hasNext()) {
			DBObject obj=cursor.next();
		    file.setChunkCount((int)obj.get("ChunkCount"));
		    file.setChunkId((int)obj.get("ChunkID"));
		    ByteString val = ByteString.copyFrom(Base64.decodeToBytes((String)obj.get("FileData")));
		    file.setData(val);
		    file.setFilename(fname);
		  data.add(file.build());
		}

		return data;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return null;
		}
	
	}
	// checking for all the chunks of file are available or not n if not then to recort the missing chunks...
	public boolean fileCheck(String filename){
		try{
		DBObject doc=new BasicDBObject();
	    doc.put("Filename", filename);
		DBCursor cursor=dbCollection.find(doc);

		if(cursor.count()==0)
		return false;
		else
			return true;
		/***ArrayList<Integer>chunksMissing = new ArrayList<>();
		for(int i=0;i<cursor.length();i++){
			DBObject obj=cursor.next();  
			if((int)obj.get("ChunkID")!=i)
			{
			present = false;
			chunksMissing.add((Integer) obj.get("ChunkID"));
			}
			
		}**/
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return false;
		}
	}
	
	public boolean duplicateCheck(String fname,int chunkId){
		try{
		BasicDBObject doc = new BasicDBObject();
		doc.append("Filename", fname);
		doc.append("ChunkID", chunkId);
		DBCursor cursor = dbCollection.find(doc);
		if(cursor.length()==0)
			return false;
		else 
			return true;
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
			return false;
		}
	
	}
	
	@Override
	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}

}
