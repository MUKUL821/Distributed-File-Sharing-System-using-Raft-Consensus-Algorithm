package gash.project.client;

public class UploadThread implements Runnable{
	ClientFunc con;
	String fileName;
	String path;
	Client client;
	
	public UploadThread(Client client , ClientFunc con, String fileName, String path) {
		super();
		this.client = client;
		this.con = con;
		this.fileName = fileName;
		this.path = path;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			client.upload(con ,  fileName, path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
