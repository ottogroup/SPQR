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
package com.ottogroup.bi.spqr.pipeline.component.emitter;

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;

/**
 * Provides a runtime environment for {@link Emitter} instances. The environment retrieves all
 * incoming {@link StreamingDataMessage} instances from the attached {@link StreamingMessageQueueConsumer}
 * and provides them to the assigned {@link Emitter} for further processing.
 * @author mnxfst
 *
 */
public class EmitterRuntimeEnvironment implements Runnable {

	/** our faithful logging facility ... ;-) */ 
	private static final Logger logger = Logger.getLogger(EmitterRuntimeEnvironment.class);

	/** reference to emitter instance being fed with incoming messages */
	private final Emitter emitter;
	/** provides read access to assigned source queue */
	private final StreamingMessageQueueConsumer queueConsumer;
	/** indicates whether the environment is still running */
	private boolean running = false;

	/**
	 * Initializes the runtime environment using the provided input
	 * @param emitter
	 * @param queueConsumer
	 * @throws RequiredInputMissingException
	 */
	public EmitterRuntimeEnvironment(final Emitter emitter, final StreamingMessageQueueConsumer queueConsumer) throws RequiredInputMissingException {
		
		///////////////////////////////////////////////////////////////////
		// validate input
		if(emitter == null)
			throw new RequiredInputMissingException("Missing required emitter");
		if(queueConsumer == null)
			throw new RequiredInputMissingException("Missing required input queue consumer");
		//
		///////////////////////////////////////////////////////////////////
		this.emitter = emitter;
		this.queueConsumer = queueConsumer;
		
		this.running = true;
		
		if(logger.isDebugEnabled())
			logger.debug("emitter runtime environment initialized [id="+emitter.getId()+"]");

	}
	
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {		
		while(running) {
			StreamingDataMessage message = this.queueConsumer.next();
			if(message != null) {
				try {
					this.emitter.onMessage(message);
				} catch(Exception e) {
					logger.error("Failed to process incoming message with emitter [id="+emitter.getId()+"']. Reason: " + e.getMessage());
				}
			} else {
				// TODO provide wait strategy
			}
		}		
	}

	/**
	 * Shuts down the runtime environment as well as the attached {@link Emitter}
	 */
	public void shutdown() {
		this.running = false;
		try {
			this.emitter.shutdown();
		} catch(Exception e) {
			logger.error("Failed to shut down emitter. Error: " + e.getMessage());
		}
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

}
