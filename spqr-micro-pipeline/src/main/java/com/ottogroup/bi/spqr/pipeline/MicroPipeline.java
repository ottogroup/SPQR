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
package com.ottogroup.bi.spqr.pipeline;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.emitter.EmitterRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperatorRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.source.SourceRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;

/**
 * Provides a runtime container for {@link MicroPipelineComponent} instances interconnected 
 * through {@link StreamingMessageQueue} instances.    
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class MicroPipeline {
	
	private static final Logger logger = Logger.getLogger(MicroPipeline.class);

	/** default identifier used for statistics queue - must not be assigned to other queues */ 
	public static final String STATISTICS_QUEUE_NAME = "stats";
	
	/** unique pipeline identifier - must be unique within the whole cluster */
	private final String id;
	/** references to source runtime environments */
	private final Map<String, SourceRuntimeEnvironment> sources = new HashMap<>();
	/** references to direct response operator runtime environments */
	private final Map<String, DirectResponseOperatorRuntimeEnvironment> directResponseOperators = new HashMap<>();
	/** references to delayed response operator runtime environments */
	private final Map<String, DelayedResponseOperatorRuntimeEnvironment> delayedResponseOperators = new HashMap<>();
	/** references to emitter runtime environments */
	private final Map<String, EmitterRuntimeEnvironment> emitters = new HashMap<>();
	/** references to queues interconnecting the components */
	private final Map<String, StreamingMessageQueue> queues = new HashMap<>();
	
	/**
	 * Initializes the micro pipeline instance using the provided input 
	 * @param id
	 */
	public MicroPipeline(final String id) {
		this.id = id;
	}
	
	/**
	 * Adds a new {@link SourceRuntimeEnvironment} 
	 * @param id
	 * @param sourceRuntimeEnvironment
	 * TODO test
	 */
	public void addSource(final String id, final SourceRuntimeEnvironment sourceRuntimeEnvironment) {
		this.sources.put(id, sourceRuntimeEnvironment);
		if(logger.isDebugEnabled())
			logger.debug("Source [id="+id+"] successfully attached to pipeline [id="+this.id+"]");
	}
	
	/**
	 * Adds a new {@link DirectResponseOperatorRuntimeEnvironment}
	 * @param id
	 * @param operatorRuntimeEnvironment
	 * TODO test
	 */
	public void addOperator(final String id, final DirectResponseOperatorRuntimeEnvironment operatorRuntimeEnvironment) {
		this.directResponseOperators.put(id, operatorRuntimeEnvironment);
		if(logger.isDebugEnabled())
			logger.debug("Direct response operator [id="+id+"] successfully attached to pipeline [id="+this.id+"]");
	}
	
	/**
	 * Adds a new {@link DelayedResponseOperatorRuntimeEnvironment}
	 * @param id
	 * @param operatorRuntimeEnvironment
	 * TODO test
	 */
	public void addOperator(final String id, final DelayedResponseOperatorRuntimeEnvironment operatorRuntimeEnvironment) {
		this.delayedResponseOperators.put(id, operatorRuntimeEnvironment);
		if(logger.isDebugEnabled())
			logger.debug("Delayed response operator [id="+id+"] successfully attached to pipeline [id="+this.id+"]");
	}
	
	/**
	 * Adds a new {@link EmitterRuntimeEnvironment}
	 * @param id
	 * @param emitterRuntimeEnvironment
	 * TODO test
	 */
	public void addEmitter(final String id, final EmitterRuntimeEnvironment emitterRuntimeEnvironment) {
		this.emitters.put(id, emitterRuntimeEnvironment);
		if(logger.isDebugEnabled())
			logger.debug("Emitter [id="+id+"] successfully attached to pipeline [id="+this.id+"]");
	}
	
	/**
	 * Adds a new {@link StreamingMessageQueue}
	 * @param id
	 * @param queue
	 * TODO test
	 */
	public void addQueue(final String id, final StreamingMessageQueue queue) {
		this.queues.put(id, queue);
		if(logger.isDebugEnabled())
			logger.debug("Queue [id="+id+"] successfully attached to pipeline [id="+this.id+"]");
	}
	
	/**
	 * Returns the {@link StreamingMessageQueue} referenced by the given id
	 * @param id
	 * @return
	 * TODO test
	 */
	public StreamingMessageQueue getQueue(final String id) {
		return this.queues.get(id);
	}
	
	/**
	 * Returns true in case a {@link StreamingMessageQueue} exists for the given id
	 * @param id
	 * @return
	 * TODO test
	 */
	public boolean hasQueue(final String id) {
		return this.queues.containsKey(id);
	}
	
	/**
	 * Returns true in case any {@link MicroPipelineComponent} exists for the given id
	 * @param id
	 * @return
	 * TODO test
	 */
	public boolean hasComponent(final String id) {
		return (this.sources.containsKey(id) || this.directResponseOperators.containsKey(id) || this.delayedResponseOperators.containsKey(id) || this.emitters.containsKey(id));
	}

	/**
	 * Shuts down the micro pipeline by first calling {@link MicroPipelineComponent#shutdown()} followed
	 * by {@link StreamingMessageQueue#shutdown()}
	 * TODO test
	 */
	public void shutdown() {
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// shutting down runtime environments: sources, operators, emitters
		for(final String srcId : this.sources.keySet()) {
			SourceRuntimeEnvironment srcEnv = this.sources.get(srcId);
			try {
				srcEnv.shutdown();
				if(logger.isDebugEnabled())
					logger.debug("Source runtime environment shut down [id="+srcId+"]");
			} catch(Exception e) {
				logger.error("Failed to shut down source runtime environment [id="+srcId+"]. Reason: " + e.getMessage());
			}
		}
		for(final String operatorId : this.directResponseOperators.keySet()) {
			DirectResponseOperatorRuntimeEnvironment operatorEnv = this.directResponseOperators.get(operatorId);
			try {
				operatorEnv.shutdown();
				if(logger.isDebugEnabled())
					logger.debug("Direct response operator runtime environment shut down [id="+operatorId+"]");
			} catch(Exception e) {
				logger.error("Failed to shut down direct response operator runtime environment [id="+operatorId+"]. Reason: " + e.getMessage());
			}
		}
		for(final String operatorId : this.delayedResponseOperators.keySet()) {
			DelayedResponseOperatorRuntimeEnvironment operatorEnv = this.delayedResponseOperators.get(operatorId);
			try {
				operatorEnv.shutdown();
				if(logger.isDebugEnabled())
					logger.debug("Delayed response operator runtime environment shut down [id="+operatorId+"]");
			} catch(Exception e) {
				logger.error("Failed to shut down delayed response operator runtime environment [id="+operatorId+"]. Reason: " + e.getMessage());
			}
		}
		for(final String emitterId : this.emitters.keySet()) {
			EmitterRuntimeEnvironment emitterEnv = this.emitters.get(emitterId);
			try {
				emitterEnv.shutdown();
				if(logger.isDebugEnabled())
					logger.debug("Emitter runtime environment shut down [id="+emitterId+"]");
			} catch(Exception e) {
				logger.error("Failed to shut down emitter runtime environment [id="+emitterId+"]. Reason: " + e.getMessage());
			}
		}
		//
		//////////////////////////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////////////////////////
		// shutting down queues
		for(String queueId : this.queues.keySet()) {
			StreamingMessageQueue queue = this.queues.get(queueId);
			try {
				queue.shutdown();
				if(logger.isDebugEnabled())
					logger.debug("Queue shut down [id="+queueId+"]");
			} catch(Exception e) {
				logger.error("Failed to shut down queue [id="+queueId+"]. Reason: " + e.getMessage());
			}
		}
		//
		//////////////////////////////////////////////////////////////////////////////////////////
		
		if(logger.isDebugEnabled())
			logger.debug("Micro pipeline [id="+id+"] successfully shut down");
	}
	
	public String getId() {
		return id;
	}

	public Map<String, SourceRuntimeEnvironment> getSources() {
		return sources;
	}

	public Map<String, DelayedResponseOperatorRuntimeEnvironment> getDelayedResponseOperators() {
		return delayedResponseOperators;
	}

	public Map<String, EmitterRuntimeEnvironment> getEmitters() {
		return emitters;
	}

	public Map<String, DirectResponseOperatorRuntimeEnvironment> getDirectResponseOperators() {
		return directResponseOperators;
	}

	public Map<String, StreamingMessageQueue> getQueues() {
		return queues;
	}
	
	
}
