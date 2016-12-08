package gash.router.server;

import gash.router.container.GlobalRoutingConf;
import gash.router.container.RoutingConf;
import gash.router.global.edges.GlobalEdgeMonitor;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.tasks.TaskList;

public class ServerState {
	private RoutingConf conf;
	private GlobalRoutingConf gConf;
	public GlobalRoutingConf getgConf() {
		return gConf;
	}

	public void setgConf(GlobalRoutingConf gConf) {
		this.gConf = gConf;
	}

	private EdgeMonitor emon;
	private TaskList tasks;
	private GlobalEdgeMonitor gEmon;
	public GlobalEdgeMonitor getgEmon() {
		return gEmon;
	}

	public void setgEmon(GlobalEdgeMonitor gEmon) {
		this.gEmon = gEmon;
	}

	public volatile static String state="Follower";
	
	/*
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
*/
	public RoutingConf getConf() {
		return conf;
	}

	public void setConf(RoutingConf conf) {
		this.conf = conf;
	}

	public EdgeMonitor getEmon() {
		return emon;
	}

	public void setEmon(EdgeMonitor emon) {
		this.emon = emon;
	}

	public TaskList getTasks() {
		return tasks;
	}

	public void setTasks(TaskList tasks) {
		this.tasks = tasks;
	}

}
