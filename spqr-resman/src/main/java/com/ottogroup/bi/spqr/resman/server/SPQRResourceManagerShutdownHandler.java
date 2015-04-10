/**
 * Copyright 2015 Otto (GmbH & Co KG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ottogroup.bi.spqr.resman.server;

import org.apache.log4j.Logger;

/**
 * Manages the resource manager shutdown
 * @author mnxfst
 * @since Apr 10, 2015
 */
public class SPQRResourceManagerShutdownHandler extends Thread {

	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRResourceManagerShutdownHandler.class);
	
	/**
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		logger.info("SPQR resource manager instance successfully shut down");
	}

	
	
}
