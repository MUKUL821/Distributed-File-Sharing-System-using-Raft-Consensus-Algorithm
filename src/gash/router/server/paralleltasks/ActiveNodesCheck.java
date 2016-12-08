package gash.router.server.paralleltasks;

import java.util.Map;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;

public 	class ActiveNodesCheck extends TimerTask{
	private static Logger logger = LoggerFactory.getLogger("ActiveNodesCheck");
	@Override
	public void run() {
		try{
		if(ServerState.state=="Leader"){
			for (Map.Entry<Integer, Boolean> entry :EdgeList.edgeStatus.entrySet()) {
				if(entry.getValue()==false){
					logger.info("getting false for"+entry.getKey());
					EdgeInfo ei = EdgeList.map.get(entry.getKey());
					EdgeList.map.get(ei.getRef()).setChannel(null);
					EdgeList.map.get(ei.getRef()).setActive(false);;
					EdgeList.activeEdges.remove(entry.getKey());
					if(EdgeList.activeEdges.size()==0){
						ServerState.state="Follower";
					}
				}
				EdgeList.edgeStatus.put(entry.getKey(), false);
			}		
		}
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred : " + e.getMessage() );
		}
	}

}