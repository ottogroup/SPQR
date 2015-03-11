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
package com.ottogroup.bi.spqr.pipeline.component.operator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;

/**
 * Provides a runtime environment for {@link DelayedResponseOperator} instances. The environment polls
 * messages from the assigned {@link StreamingMessageQueueConsumer} and forwards them for further processing
 * to the {@link DirectResponseOperator}. In case the condition evaluated by the {@link DelayedResponseOperatorWaitStrategy}
 * provided on startup holds, the environment asks the operator to return its {@link DelayedResponseOperator#getResult() results}
 * which are forwarded to the {@link StreamingMessageQueueProducer} (order is preserved as received from operator).
 * @author mnxfst
 * @since Mar 11, 2015
 */
public class DelayedResponseOperatorRuntimeEnvironment implements Runnable, DelayedResponseCollector {

	/** our faithful logging facility ... ;-) */ 
	private static final Logger logger = Logger.getLogger(DelayedResponseOperatorRuntimeEnvironment.class);

	/** operator instance executed by this runtime environment */
	private final DelayedResponseOperator delayedResponseOperator;
	/** strategy to apply when waiting for responses */
	private final DelayedResponseOperatorWaitStrategy responseWaitStrategy;
	/** provides read access to assigned source queue */
	private final StreamingMessageQueueConsumer queueConsumer;
	/** provides write access to assigned destination queue */
	private final StreamingMessageQueueProducer queueProducer;	
	/** indicates whether the operator runtime is still running or not */
	private boolean running = false;
	/** executor environment used to run the response wait strategy */
	private final ExecutorService executorService;
	/** local executor service? - must be shut down as well, otherwise the provider must take care of it */
	private boolean localExecutorService = false;

	/**
	 * Initializes the runtime environment using the provied input
	 * @param delayedResponseOperator
	 * @param responseWaitStrategy
	 * @param queueConsumer
	 * @param queueProducer
	 * @throws RequiredInputMissingException
	 */
	public DelayedResponseOperatorRuntimeEnvironment(final DelayedResponseOperator delayedResponseOperator, final DelayedResponseOperatorWaitStrategy responseWaitStrategy,
			final StreamingMessageQueueConsumer queueConsumer, final StreamingMessageQueueProducer queueProducer) throws RequiredInputMissingException {
		this(delayedResponseOperator, responseWaitStrategy, queueConsumer, queueProducer, Executors.newCachedThreadPool());
		this.localExecutorService = true;
	}
	
	/**
	 * Initializes the runtime environment using the provided input
	 * @param delayedResponseOperator
	 * @param responseWaitStrategy
	 * @param queueConsumer
	 * @param queueProducer
	 * @param executorService
	 * @throws RequiredInputMissingException
	 */
	public DelayedResponseOperatorRuntimeEnvironment(final DelayedResponseOperator delayedResponseOperator, final DelayedResponseOperatorWaitStrategy responseWaitStrategy,
			final StreamingMessageQueueConsumer queueConsumer, final StreamingMessageQueueProducer queueProducer, final ExecutorService executorService) throws RequiredInputMissingException {
		
		/////////////////////////////////////////////////////////////
		// input validation
		if(delayedResponseOperator == null)
			throw new RequiredInputMissingException("Missing required direct delayed operator");
		if(responseWaitStrategy == null)
			throw new RequiredInputMissingException("Missing required response wait strategy");
		if(queueConsumer == null)
			throw new RequiredInputMissingException("Missing required queue consumer");
		if(queueProducer == null)
			throw new RequiredInputMissingException("Missing required queue producer");
		//
		/////////////////////////////////////////////////////////////
		
		this.delayedResponseOperator = delayedResponseOperator;
		this.responseWaitStrategy = responseWaitStrategy;
		this.responseWaitStrategy.setDelayedResponseCollector(this);
		this.queueConsumer = queueConsumer;
		this.queueProducer = queueProducer;
		this.executorService = executorService;		
		this.executorService.submit(this.responseWaitStrategy);
		this.running = true;
		
		if(logger.isDebugEnabled())
			logger.debug("delayed response operator runtime environment initialized [id="+delayedResponseOperator.getId()+"]");
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		while(running) {			
			StreamingDataMessage message = this.queueConsumer.next();
			if(message != null) {
				// forward retrieved message to operator for further processing
				this.delayedResponseOperator.onMessage(message);
				// notify response wait strategy on retrieved message
				this.responseWaitStrategy.onMessage(message);
			} else {
				// TODO implement wait strategy
			}
		}
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseCollector#retrieveMessages()
	 */
	public void retrieveMessages() {		
		StreamingDataMessage[] retrievedMessages = null;
		try {
			// try to fetch messages from underlying operator
			retrievedMessages = this.delayedResponseOperator.getResult();
		} catch(Exception e) {
			logger.error("Failed to retrieve and forward messages from underlying delayed response operator. Error: " + e.getMessage());
		}

		// forward messages to assigned queue if any messages are available 
		if(retrievedMessages != null) {
			for(StreamingDataMessage rm : retrievedMessages)
				this.queueProducer.insert(rm);
		}

	}

	/**
	 * Shuts down the runtime environment as well as the attached {@link Operator}
	 */
	public void shutdown() {
		this.running = false;
		try {
			this.delayedResponseOperator.shutdown();
		} catch(Exception e) {
			logger.error("Failed to shut down delayed response operator. Error: " + e.getMessage());
		}
		try {
			this.responseWaitStrategy.shutdown();
		} catch(Exception e) {
			logger.error("Failed to shut down response wait strategy. Error: " + e.getMessage());
		}
		if(this.localExecutorService) {
			try {
				this.executorService.shutdownNow();
			} catch(Exception e) {
				logger.error("Failed to shut down executor service. Error: " + e.getMessage());
			}
		}


		if(logger.isDebugEnabled())
			logger.debug("delayed response operator runtime environment shutdown [id="+this.delayedResponseOperator.getId()+"]");

	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}
}
