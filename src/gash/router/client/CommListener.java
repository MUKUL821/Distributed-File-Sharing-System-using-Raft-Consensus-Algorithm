/*
 * copyright 2016, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.client;

import routing.Pipe.CommandMessage;

public interface CommListener {
	/**
	 * identifies the listener - if it needs to be removed or tracked
	 * 
	 * @return
	 */
	public abstract String getListenerID();

	/**
	 * receives the message event from the client's channel
	 * 
	 * @param msg
	 *            Both requests and responses are held in the same message
	 *            structure
	 */
	public abstract void onMessage(Object msg);
}
