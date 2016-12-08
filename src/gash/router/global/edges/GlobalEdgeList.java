/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.global.edges;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalEdgeList {
	public static volatile ConcurrentHashMap<String, GlobalEdgeInfo> map = new ConcurrentHashMap<String, GlobalEdgeInfo>();

	public static volatile ConcurrentHashMap<Integer, Boolean>  edgeStatus = new ConcurrentHashMap<Integer, Boolean>  ();
	
	public static volatile ConcurrentHashMap<Integer, GlobalEdgeInfo> activeEdges = new ConcurrentHashMap<Integer, GlobalEdgeInfo>();
//public static volatile HashMap<Integer, EdgeInfo>  inactiveEdges = new HashMap<Integer, EdgeInfo>  ();
	
//	public static HashMap<Integer, EdgeInfo> inactiveEdgesMap = new HashMap<Integer, EdgeInfo>();

	public GlobalEdgeList() {
	}

	public GlobalEdgeInfo createIfNew(int ref, String host, int port) {
		if (GlobalhasNode(ref))
			return getNode(ref);
		else
			return addNode(ref, host, port);
	}

	public GlobalEdgeInfo addNode(int ref, String host, int port) {
		if (!verify(ref, host, port)) {
			// TODO log error
			throw new RuntimeException("Invalid node info");
		}

		if (!GlobalhasNode(ref)) {
			GlobalEdgeInfo ei = new GlobalEdgeInfo(ref, host, port);
			map.put(host, ei);
			activeEdges.put(ref, ei);
			edgeStatus.put(ref,true);
			return ei;
		} else
			return null;
	}

	private boolean verify(int ref, String host, int port) {
		if (ref < 0 || host == null || port < 1024)
			return false;
		else
			return true;
	}

	public boolean GlobalhasNode(int ref) {
		return map.containsKey(ref);

	}

	public GlobalEdgeInfo getNode(int ref) {
		return map.get(ref);
	}

	public void removeNode(int ref) {
		map.remove(ref);
	}

	public void clear() {
		map.clear();
	}
}
