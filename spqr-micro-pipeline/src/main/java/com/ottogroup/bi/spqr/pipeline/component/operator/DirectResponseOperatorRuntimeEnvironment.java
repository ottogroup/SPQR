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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Provides a runtime environment for {@link DirectResponseOperator} instances. The environment polls
 * messages from the assigned {@link StreamingMessageQueueConsumer}, forwards them for further processing
 * to the {@link DirectResponseOperator} and inserts all generated {@link StreamingDataMessage response messages}
 * into the {@link StreamingMessageQueueProducer}. The message order as received from the operator is 
 * preserved when handing over the messages to the queue producer.
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class DirectResponseOperatorRuntimeEnvironment implements Runnable {

	/** our faithful logging facility ... ;-) */ 
	private static final Logger logger = Logger.getLogger(DirectResponseOperatorRuntimeEnvironment.class);

	/** identifier of processing node the runtime environment belongs to*/
	private final String processingNodeId;
	/** identifier of pipeline the runtime environment belongs to */
	private final String pipelineId;
	/** identifier of operator assigned to this runtime environment */
	private final String operatorId; 
	/** operator instance executed by this runtime environment */
	private final DirectResponseOperator directResponseOperator;
	/** provides read access to assigned source queue */
	private final StreamingMessageQueueConsumer queueConsumer;
	/** provides write access to assigned destination queue */
	private final StreamingMessageQueueProducer queueProducer;
	/** indicates whether the operator runtime is still running or not */
	private boolean running = false;
	/** consumer queue wait strategy */
	private final StreamingMessageQueueWaitStrategy consumerQueueWaitStrategy;
	/** destination queue wait strategy */
	private final StreamingMessageQueueWaitStrategy destinationQueueWaitStrategy;
	/** message counter metric */
	private Counter messageCounter = null;
	/** message processing timer metric */
	private Timer messageProcessingTimer = null;


	/**
	 * Initializes the operator runtime environment using the provided input
	 * @param processingNodeId
	 * @param pipelineId
	 * @param directResponseOperator
	 * @param queueConsumer
	 * @param queueProducer
	 */
	public DirectResponseOperatorRuntimeEnvironment(final String processingNodeId, final String pipelineId, final DirectResponseOperator directResponseOperator, final StreamingMessageQueueConsumer queueConsumer, 
			final StreamingMessageQueueProducer queueProducer) throws RequiredInputMissingException {
		
		/////////////////////////////////////////////////////////////
		// input validation
		if(StringUtils.isBlank(processingNodeId))
			throw new RequiredInputMissingException("Missing required processing node identifier");
		if(StringUtils.isBlank(pipelineId))
			throw new RequiredInputMissingException("Missing required pipeline identifier");
		if(directResponseOperator == null)
			throw new RequiredInputMissingException("Missing required direct response operator");
		if(queueConsumer == null)
			throw new RequiredInputMissingException("Missing required queue consumer");
		if(queueProducer == null)
			throw new RequiredInputMissingException("Missing required queue producer");
		//
		/////////////////////////////////////////////////////////////
		
		this.processingNodeId = StringUtils.lowerCase(StringUtils.trim(processingNodeId));
		this.pipelineId = StringUtils.lowerCase(StringUtils.trim(pipelineId));
		this.operatorId = StringUtils.lowerCase(StringUtils.trim(directResponseOperator.getId()));
		this.directResponseOperator = directResponseOperator;
		this.queueConsumer = queueConsumer;
		this.queueProducer = queueProducer;
		this.running = true;
		this.consumerQueueWaitStrategy = queueConsumer.getWaitStrategy();
		this.destinationQueueWaitStrategy = queueProducer.getWaitStrategy();

		if(logger.isDebugEnabled())
			logger.debug("direct response operator init [node="+this.processingNodeId+", pipeline="+this.pipelineId+", operator="+this.operatorId+"]");
	}
		
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		while(running) {
			
			try {				
				StreamingDataMessage message = this.consumerQueueWaitStrategy.waitFor(this.queueConsumer);
				if(message != null && message.getBody() != null) {
					
					@SuppressWarnings("resource") // context#close() calls context#stop -> avoid additional call, thus accept warning
					Timer.Context timerContext = (this.messageProcessingTimer != null ? this.messageProcessingTimer.time() : null);

					StreamingDataMessage[] responseMessages = this.directResponseOperator.onMessage(message);
					if(responseMessages != null && responseMessages.length > 0) {
						for(final StreamingDataMessage responseMessage : responseMessages) {
							this.queueProducer.insert(responseMessage);
						}
						this.destinationQueueWaitStrategy.forceLockRelease();
					}
					
					if(timerContext != null)
						timerContext.stop();

					if(this.messageCounter != null)
						this.messageCounter.inc();
				}
				
			} catch(InterruptedException e) {
				// do nothing - waiting was interrupted				
			} catch(Exception e) {
				logger.error("processing error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", operator="+this.operatorId+"]: " + e.getMessage(), e);
				// TODO add handler for responding to errors 
			}
		}		
	}
	
	/**
	 * Shuts down the runtime environment as well as the attached {@link Operator}
	 */
	public void shutdown() {
		this.running = false;
		try {
			this.directResponseOperator.shutdown();
		} catch(Exception e) {
			logger.error("operator shutdown error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", operator="+this.operatorId+"]: " + e.getMessage(), e);
		}

		if(logger.isDebugEnabled())
			logger.debug("shutdown success [node="+this.processingNodeId+", pipeline="+this.pipelineId+", operator="+this.operatorId+"]");

	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param messageCounter the messageCounter to set
	 */
	public void setMessageCounter(Counter messageCounter) {
		this.messageCounter = messageCounter;
	}

	/**
	 * @param messageProcessingTimer the messageProcessingTimer to set
	 */
	public void setMessageProcessingTimer(Timer messageProcessingTimer) {
		this.messageProcessingTimer = messageProcessingTimer;
	}
	
}
