package gash.router.server.edges;

public class LeaderDetails {
	public static volatile int term = 0;
	public static volatile int id =-1;
	public static volatile EdgeInfo ei = null;
	public volatile static boolean receivedHBFromLeader = false;
	public static int getTerm() {
		return term;
	}
	public static void setTerm(int term) {
		LeaderDetails.term = term;
	}
	public static int getId() {
		return id;
	}
	public static void setId(int id) {
		LeaderDetails.id = id;
	}
	public static EdgeInfo getEdge() {
		return ei;
	}
	public static void setEdge(EdgeInfo edge) {
		LeaderDetails.ei = edge;
	}
	public static boolean isLeaderDetailsSet(){
		if(id!=-1&&ei!=null)
			return true;
		else 
			return false;
	}
	public static void setLeaderDetailsDefault(){
		id=-1;
		ei=null;
	}
}
