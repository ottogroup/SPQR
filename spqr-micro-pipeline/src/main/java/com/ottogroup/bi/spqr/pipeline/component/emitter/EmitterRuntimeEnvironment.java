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

import java.util.Timer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.statistics.AggregatedComponentStatistics;
import com.ottogroup.bi.spqr.pipeline.statistics.ComponentStatsSupport;
import com.ottogroup.bi.spqr.pipeline.statistics.ComponentStatsTriggerTask;

/**
 * Provides a runtime environment for {@link Emitter} instances. The environment retrieves all
 * incoming {@link StreamingDataMessage} instances from the attached {@link StreamingMessageQueueConsumer}
 * and provides them to the assigned {@link Emitter} for further processing.
 * @author mnxfst
 *
 */
public class EmitterRuntimeEnvironment implements Runnable, ComponentStatsSupport {

	/** our faithful logging facility ... ;-) */ 
	private static final Logger logger = Logger.getLogger(EmitterRuntimeEnvironment.class);

	/** identifier of processing node the runtime environment belongs to*/
	private final String processingNodeId;
	/** identifier of pipeline the runtime environment belongs to */
	private final String pipelineId;
	/** identifier of emitter assigned to this runtime environment */
	private final String emitterId; 
	/** reference to emitter instance being fed with incoming messages */
	private final Emitter emitter;
	/** provides read access to assigned source queue */
	private final StreamingMessageQueueConsumer queueConsumer;
	/** attached statistics queue producer */
	private final StreamingMessageQueueProducer statsQueueProducer;
	/** indicates whether the environment is still running */
	private boolean running = false;
	/** stats counter */
	private final AggregatedComponentStatistics statsEvent;
	/** timer triggering stats collection */
	private final Timer statsCollectionTimer;


	/**
	 * Initializes the runtime environment using the provided input
	 * @param processingNodeId
	 * @param pipelineId
	 * @param emitter
	 * @param queueConsumer
	 * @throws RequiredInputMissingException
	 */
	public EmitterRuntimeEnvironment(final String processingNodeId, final String pipelineId, final Emitter emitter, 
			final StreamingMessageQueueConsumer queueConsumer, final StreamingMessageQueueProducer statsQueueProducer,
			final long statsCollectionDelay) throws RequiredInputMissingException {
		
		///////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(processingNodeId))
			throw new RequiredInputMissingException("Missing required processing node identifier");
		if(StringUtils.isBlank(pipelineId))
			throw new RequiredInputMissingException("Missing required pipeline identifier");
		if(emitter == null)
			throw new RequiredInputMissingException("Missing required emitter");
		if(queueConsumer == null)
			throw new RequiredInputMissingException("Missing required input queue consumer");
		if(statsQueueProducer == null) 
			throw new RequiredInputMissingException("Missing required stats queue producer");
		//
		///////////////////////////////////////////////////////////////////

		this.processingNodeId = StringUtils.lowerCase(StringUtils.trim(processingNodeId));
		this.pipelineId = StringUtils.lowerCase(StringUtils.trim(pipelineId));
		this.emitterId = StringUtils.lowerCase(StringUtils.trim(emitter.getId()));
		this.emitter = emitter;
		this.queueConsumer = queueConsumer;
		this.statsQueueProducer = statsQueueProducer;
		
		this.statsEvent = new AggregatedComponentStatistics(this.processingNodeId, this.pipelineId, this.emitterId, 0, 0, 
				0, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 0);
		this.statsEvent.start();
		
		this.statsCollectionTimer = new Timer(true);
		this.statsCollectionTimer.schedule(new ComponentStatsTriggerTask(this), (statsCollectionDelay > 0 ? statsCollectionDelay : 1000), (statsCollectionDelay > 0 ? statsCollectionDelay : 1000));

		this.running = true;
		
		if(logger.isDebugEnabled())
			logger.debug("emitter init [node="+this.processingNodeId+", pipeline="+this.pipelineId+", emitter="+this.emitterId+"]");

	}
		
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		// initialize stats counter and timer
		long start = 0;
		int bodySize = 0;

		// fetch the wait strategy attached to the queue (provided through the queue consumer)
		StreamingMessageQueueWaitStrategy queueWaitStrategy = this.queueConsumer.getWaitStrategy();
		while(running) {

			start = 0;
			bodySize = 0;
			try {
				// fetch message from queue consumer via strategy
				StreamingDataMessage message = queueWaitStrategy.waitFor(this.queueConsumer);
				start = System.currentTimeMillis();
				if(message != null && message.getBody() != null) {
					this.emitter.onMessage(message);
					bodySize = message.getBody().length;
				} 

				////////////////////////////////////////////////////////
				// stats handling
				statsEvent.addEvent((int)(System.currentTimeMillis()-start), bodySize, false);
				////////////////////////////////////////////////////////
			} catch(InterruptedException e) {
				// do nothing - waiting was interrupted				
			} catch(Exception e) {
				logger.error("processing error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", emitter="+this.emitterId+"]: " + e.getMessage(), e);
				// TODO add handler for responding to errors
				
				////////////////////////////////////////////////////////
				// stats handling
				statsEvent.addEvent((int)(System.currentTimeMillis()-start), bodySize, true);
				////////////////////////////////////////////////////////
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
			if(logger.isDebugEnabled())
				logger.debug("emitter shutdown [node="+this.processingNodeId+", pipeline="+this.pipelineId+", emitter="+this.emitterId+"]");
		} catch(Exception e) {
			logger.error("emitter shutdown error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", emitter="+this.emitterId+"]: " + e.getMessage(), e);
		}

	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.statistics.ComponentStatsSupport#collectAndForwardStats()
	 */
	public void collectAndForwardStats() {
		this.statsEvent.finish();
		this.statsQueueProducer.insert(new StreamingDataMessage(this.statsEvent.toBytes(), System.currentTimeMillis()));
		this.statsQueueProducer.getWaitStrategy().forceLockRelease();
		this.statsEvent.start();
	}

}
