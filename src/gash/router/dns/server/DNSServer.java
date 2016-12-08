package gash.router.dns.server;

import pipe.work.Work.WorkMessage;

public class DNSServer {
	
	public static volatile WorkMessage.Builder wm = WorkMessage.newBuilder();
		public static void main(String[] args) {
			
			DNSMappingServer  dsh =new DNSMappingServer ();
			
		}

	}

