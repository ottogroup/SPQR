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
package com.ottogroup.bi.spqr.pipeline.component.source;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.statistics.ComponentMessageStatsEvent;

/**
 * Runtime environment for {@link Source} instances
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class SourceRuntimeEnvironment implements Runnable, IncomingMessageCallback {

	/** our faithful logging facility ... ;-) */ 
	private static final Logger logger = Logger.getLogger(SourceRuntimeEnvironment.class);
	
	/** identifier of processing node the runtime environment belongs to*/
	private final String processingNodeId;
	/** identifier of pipeline the runtime environment belongs to */
	private final String pipelineId;
	/** identifier of source assigned to this runtime environment */
	private final String sourceId; 
	/** source instances executed by this runtime environment */
	private final Source source;
	/** attached queue producer which receives incoming messages */
	private final StreamingMessageQueueProducer queueProducer;
	/** attached statistics queue producer */
	private final StreamingMessageQueueProducer statsQueueProducer;
	/** executor service that will run the source instance */
	private final ExecutorService executorService;
	/** local executor service? - must be shut down as well, otherwise the provider must take care of it */
	private boolean localExecutorService = false;
	/** stats container */
	private final ComponentMessageStatsEvent statsEvent;

	/**
	 * Initializes the runtime environment using the provided input
	 * @param processingNodeId
	 * @param pipelineId
	 * @param source
	 * @param queueProducer
	 * @param statsQueueProducer
	 * @throws RequiredInputMissingException
	 */
	public SourceRuntimeEnvironment(final String processingNodeId, final String pipelineId, final Source source, final StreamingMessageQueueProducer queueProducer, final StreamingMessageQueueProducer statsQueueProducer) throws RequiredInputMissingException {
		this(processingNodeId, pipelineId, source, queueProducer, statsQueueProducer, Executors.newCachedThreadPool());
		this.localExecutorService = true;
	}
	
	/**
	 * Initializes the runtime environment using the provided input
	 * @param processingNodeId
	 * @param pipelineId
	 * @param source
	 * @param queueProducer
	 * @param statsQueueProducer
	 * @param executorService
	 * @throws RequiredInputMissingException
	 */
	public SourceRuntimeEnvironment(final String processingNodeId, final String pipelineId, final Source source, final StreamingMessageQueueProducer queueProducer, final StreamingMessageQueueProducer statsQueueProducer, final ExecutorService executorService) throws RequiredInputMissingException {
		
		///////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(processingNodeId))
			throw new RequiredInputMissingException("Missing required processing node identifier");
		if(StringUtils.isBlank(pipelineId))
			throw new RequiredInputMissingException("Missing required pipeline identifier");
		if(source == null)
			throw new RequiredInputMissingException("Missing required source");
		if(queueProducer == null)
			throw new RequiredInputMissingException("Missing required queue producer");
		if(statsQueueProducer == null)
			throw new RequiredInputMissingException("Missing required stats queue producer");
		if(executorService == null)
			throw new RequiredInputMissingException("Missing required executor service");
		//
		///////////////////////////////////////////////////////////////
		
		this.processingNodeId = StringUtils.lowerCase(StringUtils.trim(processingNodeId));
		this.pipelineId = StringUtils.lowerCase(StringUtils.trim(pipelineId));
		this.sourceId = StringUtils.lowerCase(StringUtils.trim(source.getId()));
		this.source = source;
		this.source.setIncomingMessageCallback(this);
		this.queueProducer = queueProducer;
		this.statsQueueProducer = statsQueueProducer;
		this.executorService = executorService;
		
		this.statsEvent = new ComponentMessageStatsEvent(this.sourceId, false, 0, 0, 0);
		
		this.executorService.submit(source);
		if(logger.isDebugEnabled())
			logger.debug("source runtime environment initialized [id="+source.getId()+"]");
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
		long start = System.currentTimeMillis();
		int bodySize = (message != null && message.getBody() != null ? message.getBody().length : 0);
		this.queueProducer.insert(message);
		this.queueProducer.getWaitStrategy().forceLockRelease();

		////////////////////////////////////////////////////////
		// prepare and export stats message
		statsEvent.setDuration((int)(System.currentTimeMillis()-start));
		statsEvent.setError(false);
		statsEvent.setSize(bodySize);
		statsEvent.setTimestamp(System.currentTimeMillis());
		this.statsQueueProducer.insert(new StreamingDataMessage(statsEvent.toBytes(), System.currentTimeMillis()));
		////////////////////////////////////////////////////////
	}
	
	/**
	 * Shuts down the runtime environment as well as the attached {@link Source}
	 */
	public void shutdown() {
		
		try {
			this.source.shutdown();
		} catch(Exception e) {
			logger.error("source shutdown error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", source="+this.sourceId+"]: " + e.getMessage(), e);
		}
		
		if(this.localExecutorService) {
			try {
				this.executorService.shutdownNow();
			} catch(Exception e) {
				logger.error("exec service shutdown error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", source="+this.sourceId+"]: " + e.getMessage(), e);
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("source shutdown [node="+this.processingNodeId+", pipeline="+this.pipelineId+", source="+this.sourceId+"]");
	}
	
}
