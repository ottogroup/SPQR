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
package com.ottogroup.bi.spqr.node.server;

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.pipeline.MicroPipelineManager;

/**
 * @author mnxfst
 * @since Mar 17, 2015
 */
public class SPQRNodeShutdownHandler extends Thread {

	/** our faithful logging service ... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRNodeShutdownHandler.class);
	
	private final MicroPipelineManager microPipelineManager;
	
	/**
	 * Initializes the shutdown handler by assigning the provided {@link MicroPipelineManager}
	 * which is used on shutdown to stop all running micro pipelines
	 * @param microPipelineManager
	 */
	public SPQRNodeShutdownHandler(final MicroPipelineManager microPipelineManager) {
		this.microPipelineManager = microPipelineManager;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		logger.info("Preparing to shut down node");
		this.microPipelineManager.shutdown();
		logger.info("All running pipelines shut down...");		
	}

}
