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
package gash.router.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pipe.common.Common.Failure;
import pipe.common.Common.Header;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;
import routing.Pipe.GlobalHeader;

public class PrintUtil {
	private static final String gap = "   ";
	protected static Logger logger = LoggerFactory.getLogger("printmessageDetail");
	public static void printHeader(Header hdr) {
		System.out.println("\n-------------------------------------------------------");
		System.out.println("ID:   " + hdr.getNodeId());
		System.out.println("Time: " + hdr.getTime());
		if (hdr.hasMaxHops())
			System.out.println("Hops: " + hdr.getMaxHops());
		if (hdr.hasDestination())
			System.out.println("Dest: " + hdr.getDestination());

	}

	public static void printGHeader(GlobalHeader globalHeader) {
		System.out.println("\n-------------------------------------------------------");
		System.out.println("ID:   " + globalHeader.getClusterId());
		System.out.println("Time: " + globalHeader.getTime());
		if (globalHeader.hasMaxHops())
			System.out.println("Hops: " + globalHeader.getMaxHops());
		if (globalHeader.hasDestinationId())
			System.out.println("Dest: " + globalHeader.getDestinationId());

	}

	public static void printCommand(CommandMessage msg) {
		PrintUtil.printGHeader(msg.getGlobalHeader());
         logger.info("in printuti");
		System.out.print("\nCommand: ");
		if (msg.hasFailure()) {
			System.out.println("Failure");
			System.out.println(PrintUtil.gap + "Code:    " + msg.getFailure().getId());
			System.out.println(PrintUtil.gap + "Ref ID:  " + msg.getFailure().getRefId());
			System.out.println(PrintUtil.gap + "Message: " + msg.getFailure().getMessage());
		} else if (msg.hasPing())
			System.out.println("Ping");
		else if (msg.hasMessage()) {
			System.out.println("Message");
			System.out.println(PrintUtil.gap + "Msg:  " + msg.getMessage());
		} else
			System.out.println("Unknown operation"); 
	}

	public static void printWork(WorkMessage msg) {
		PrintUtil.printHeader(msg.getHeader());

		System.out.print("\nWork: ");
		if (msg.hasErr())
			System.out.println("Failure");
		else if (msg.hasPing())
			System.out.println("Ping");
		else
			System.out.println("Unknown");

		System.out.println(PrintUtil.gap + "Sec:  " + msg.getSecret());
	}

	public static void printFailure(Failure f) {
		System.out.println("ERROR: " + f.getId() + "." + f.getRefId() + " : " + f.getMessage());
	}
}
