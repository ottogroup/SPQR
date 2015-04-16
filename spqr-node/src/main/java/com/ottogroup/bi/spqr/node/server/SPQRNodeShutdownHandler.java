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

import com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient;
import com.ottogroup.bi.spqr.pipeline.MicroPipeline;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineManager;

/**
 * Invoked on application shutdown the handler ensures that all resources are properly released,
 * all {@link MicroPipeline} instances shut down and the node de-registered from the resource manager.
 * @author mnxfst
 * @since Mar 17, 2015
 */
public class SPQRNodeShutdownHandler extends Thread {

	/** our faithful logging service ... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRNodeShutdownHandler.class);
	
	private final MicroPipelineManager microPipelineManager;
	private final SPQRResourceManagerClient resourceManagerClient;
	private final String nodeId;
	
	/**
	 * Initializes the shutdown handler using the provided input
	 * @param microPipelineManager
	 * @param resourceManagerClient
	 * @param nodeId
	 */
	public SPQRNodeShutdownHandler(final MicroPipelineManager microPipelineManager, final SPQRResourceManagerClient resourceManagerClient, final String nodeId) {
		this.microPipelineManager = microPipelineManager;
		this.resourceManagerClient = resourceManagerClient;
		this.nodeId = nodeId;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		logger.info("Preparing to shut down node");
		
		try {
			this.microPipelineManager.shutdown();
			logger.info("All running pipelines shut down...");
		} catch(Exception e) {
			logger.info("Error while shutting down running pipelines: " + e.getMessage());
		}
		
		if(this.resourceManagerClient != null) {
			try {
				this.resourceManagerClient.deregisterNode(nodeId);
				logger.info("Node de-registered from resource manager");
			} catch(Exception e) {
				logger.error("Error while de-registering node from resource manager: " + e.getMessage());
			}
		}
	}
}
