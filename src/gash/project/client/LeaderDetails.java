package gash.project.client;

public class LeaderDetails {
	public static String host = "192.100.100.100";
	public static int port = 4568;
	
	public static void update(String host , int port)
	{
		LeaderDetails.host = host;
		LeaderDetails.port = port;
	}
}
