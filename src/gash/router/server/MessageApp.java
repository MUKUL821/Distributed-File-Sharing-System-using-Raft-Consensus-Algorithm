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
package gash.router.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gash1
 * 
 */
public class MessageApp {
	private static Logger logger = LoggerFactory.getLogger("MessageApp");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*if (args.length == 0) {
			logger.info("usage: server <config file>");
			System.exit(1);
		}*/
		try {
		/*********Config file location in each server .. Uncomment the file for your server**********/
		
	//	String localConfigFile = "/home/saikrishnan/work/BitBucket/fluffyglobal/runtime/route-5.conf";
		
		String localConfigFile = "C:\\Users\\mukul\\git\\fluffyglobal\\runtime\\route-1.conf";
	//	String localConfigFile = "/home/bala/bitbucket/demo/fluffyglobal/runtime/route-2.conf";
	//	String localConfigFile = "C:\\users\\sivakumar\\git\\fluffyrefactored\\runtime\\route-1.conf";
	//  String localConfigFile = "C:\\Users\\Jvalant\\bitbucket\\fluffyglobal\\runtime\\route-1.conf";
		//File cf = new File(args[0]);
	//	String globalConfigFile = "/home/saikrishnan/work/BitBucket/fluffyglobal/runtime/global-config.conf";
		//  String globalConfigFile = "C:\\Users\\Jvalant\\bitbucket\\fluffyglobal\\runtime\\global-config.conf";		
		//  String globalConfigFile = "/home/bala/bitbucket/demo/fluffyglobal/runtime/global-config.conf";
		String globalConfigFile = "C:\\Users\\mukul\\git\\fluffyglobal\\runtime\\global-config.conf";
		  
		File cfg = new File(localConfigFile);
		File gcfg = new File(globalConfigFile);	
			MessageServer svr = new MessageServer(cfg,gcfg);
			svr.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("server closing");
		}
	}
}
