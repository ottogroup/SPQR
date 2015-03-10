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
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.pipeline.component.source.SourceRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConfiguration;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;
import com.ottogroup.bi.spqr.repository.ComponentRepository;

/**
 * Manages the instantiation of {@link MicroPipeline micro pipelines} 
 * @author mnxfst
 * @since Mar 6, 2015
 */
public class MicroPipelineFactory {

	/** our faithful logging service ... ;-) */
	private static final Logger logger = Logger.getLogger(MicroPipelineFactory.class);
	/** reference towards component repository */
	private final ComponentRepository componentRepository;
	
	/**
	 * Initializes the factory using the provided input
	 * @param componentRepository
	 */
	public MicroPipelineFactory(final ComponentRepository componentRepository) {
		this.componentRepository = componentRepository;
	}
	
	
	/**
	 * Instantiates the {@link MicroPipeline} according to the provided {@link MicroPipelineComponentConfiguration} 
	 * @param cfg
	 * @return
	 * @throws RequiredInputMissingException
	 */
	public MicroPipeline instantiatePipeline(final MicroPipelineConfiguration cfg) throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInitializationFailedException {
		
		///////////////////////////////////////////////////////////////////////////////////
		// validate input
		if(cfg == null)
			throw new RequiredInputMissingException("Missing required configuration");		
		if(StringUtils.isBlank(cfg.getId()))
			throw new RequiredInputMissingException("Missing required micro pipeline id");
		if(cfg.getComponents() == null || cfg.getComponents().isEmpty())
			throw new RequiredInputMissingException("Missing required component configurations");
		if(cfg.getQueues() == null || cfg.getQueues().isEmpty())
			throw new RequiredInputMissingException("Missing required queue configurations");
		//
		///////////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////////
		// (1) initialize queues
		
		// keep track of all ready created queue instances and create a new one for each configuration
		// entry. if creation fails for any reason, all previously created queues are shut down and
		// a queue initialization exception is thrown
		MicroPipeline microPipeline = new MicroPipeline(StringUtils.lowerCase(StringUtils.trim(cfg.getId())));
		for(final StreamingMessageQueueConfiguration queueConfig : cfg.getQueues()) {
			String id = StringUtils.lowerCase(StringUtils.trim(queueConfig.getId()));
			
			// a queue for that identifier already exists: kill the pipeline and tell the caller about it
			if(microPipeline.hasQueue(id)) {
				logger.error("queue initialization failed [id="+id+"]. Forcing shutdown of all queues.");
				microPipeline.shutdown();
				throw new QueueInitializationFailedException("Non-unique queue identifier found [id="+id+"]");
			}
		
			// try to instantiate the queue, if it fails .... shutdown queues initialized so far and throw an exception
			try {
				microPipeline.addQueue(id, initializeQueue(queueConfig));			
				logger.info("queue initialized[id="+id+"]");
			} catch(Exception e) {
				logger.error("queue initialization failed [id="+id+"]. Forcing shutdown of all queues.");
				microPipeline.shutdown();
				throw new QueueInitializationFailedException("Failed to initialize queue [id="+id+"]. Reason: " + e.getMessage(), e);
			}
		}
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// (2) initialize components
		final Map<String, MicroPipelineComponent> components = new HashMap<>();
		for(final MicroPipelineComponentConfiguration componentCfg : cfg.getComponents()) {
			String id = StringUtils.lowerCase(StringUtils.trim(componentCfg.getId()));
			
			// a component for that identifier already exists: kill the pipeline and tell the caller about it
			if(microPipeline.hasComponent(id)) {
				logger.error("component initialization failed [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Forcing shutdown of all queues and components.");
				microPipeline.shutdown();
				throw new ComponentInitializationFailedException("Non-unique component identifier found [id="+id+"]");
			}
			
			// try to instantiate component, if it fails .... shutdown queues and components initialized so far and throw an exception
			try {
				MicroPipelineComponent component = initializeComponent(componentCfg, microPipeline.getQueues());
				if(component.getType() == null) {
					logger.error("component initialization failed [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Type missing. Forcing shutdown of all queues and components.");
					microPipeline.shutdown();
					throw new ComponentInitializationFailedException("Failed to initialize component [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Reason: type missing");
				}
				
				switch(component.getType()) {
					case SOURCE: {
						// fetch the (one and only) output queue from micro pipeline and attach its producer
						microPipeline.addSource(id, new SourceRuntimeEnvironment((Source)component, microPipeline.getQueue(componentCfg.getToQueues().iterator().next()).getProducer()));
						break;
					}
					case OPERATOR: {
						break;
					}
					case EMITTER: {
						break;
					}
				}
				
				components.put(id, component);
			} catch(Exception e) {
				logger.error("component initialization failed [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Forcing shutdown of all queues and components.");
				microPipeline.shutdown();
				throw new ComponentInitializationFailedException("Failed to initialize component [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Reason: " + e.getMessage(), e);
			}
		}
		///////////////////////////////////////////////////////////////////////////////////
		
		return microPipeline;
	}
	
	/**
	 * Initializes a {@link StreamingMessageQueue} instance according to provided information.
	 * @param queueConfiguration+
	 * @return
	 * @throws RequiredInputMissingException
	 * @throws QueueInitializationFailedException
	 */
	protected StreamingMessageQueue initializeQueue(final StreamingMessageQueueConfiguration queueConfiguration) throws RequiredInputMissingException, QueueInitializationFailedException {

		///////////////////////////////////////////////////////////////////////////////////
		// validate input
		if(queueConfiguration == null)
			throw new RequiredInputMissingException("Missing required queue configuration");
		if(StringUtils.isBlank(queueConfiguration.getId()))
			throw new RequiredInputMissingException("Missing required queue identifier");
		//
		///////////////////////////////////////////////////////////////////////////////////

		try {
			StreamingMessageQueue queue = new DefaultStreamingMessageQueue(); 
			queue.setId(StringUtils.lowerCase(StringUtils.trim(queueConfiguration.getId())));
			queue.initialize((queueConfiguration.getProperties() != null ? queueConfiguration.getProperties() : new Properties()));
			return queue;
		} catch(Exception e) {
			throw new QueueInitializationFailedException("Failed to initialize streaming message queue '"+queueConfiguration.getId()+"'. Error: " + e.getMessage());
		}
	}
	
	/**
	 * Initializes a {@link MicroPipelineComponent} instance according to provided information
	 * @param componentConfiguration
	 * @return
	 * @throws RequiredInputMissingException
	 * @throws ComponentInitializationFailedException
	 * TODO test for settings that must be provided for type SOURCE
	 * TODO test for settings that must be provided for type EMITTER
	 * TODO test for settings that must be provided for type OPERATOR
	 * TODO test queue references in toQueues and fromQueues
	 * TODO test component instantiation
	 */
	protected MicroPipelineComponent initializeComponent(final MicroPipelineComponentConfiguration componentConfiguration, final Map<String, StreamingMessageQueue> queues) throws RequiredInputMissingException, ComponentInitializationFailedException {
	
		///////////////////////////////////////////////////////////////////////////////////
		// validate input
		if(componentConfiguration == null)
			throw new RequiredInputMissingException("Missing required component configuration");
		if(StringUtils.isBlank(componentConfiguration.getId()))
			throw new RequiredInputMissingException("Missing required component identifier");
		if(componentConfiguration.getType() == null) 
			throw new RequiredInputMissingException("Missing required component type");
		if(StringUtils.isBlank(componentConfiguration.getName()))
			throw new RequiredInputMissingException("Missing required component name");
		if(StringUtils.isBlank(componentConfiguration.getVersion()))
			throw new RequiredInputMissingException("Missing required component version");
		if(componentConfiguration.getSettings() == null)
			throw new RequiredInputMissingException("Missing required component settings");
		//
		////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: SOURCE
		if(componentConfiguration.getType() == MicroPipelineComponentType.SOURCE) {
			
			// TODO only ONE output queue is accepted
			
			if(componentConfiguration.getToQueues() == null || componentConfiguration.getToQueues().isEmpty())
				throw new RequiredInputMissingException("Missing required queues to write content to");
			if(componentConfiguration.getToQueues().size() > 1)
				throw new RequiredInputMissingException("Source components are permitted to write content to only one queue");
			for(String toQueueId : componentConfiguration.getToQueues()) {
				if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(toQueueId))))
					throw new RequiredInputMissingException("Unknown destination queue '"+toQueueId+"' referenced");
			}
		 
		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: OPERATOR
		} else if(componentConfiguration.getType() == MicroPipelineComponentType.OPERATOR) {

			if(componentConfiguration.getToQueues() == null || componentConfiguration.getToQueues().isEmpty())
				throw new RequiredInputMissingException("Missing required queues to write content to");
			for(String toQueueId : componentConfiguration.getToQueues()) {
				if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(toQueueId))))
					throw new RequiredInputMissingException("Unknown destination queue '"+toQueueId+"' referenced");
			}
			
			if(componentConfiguration.getFromQueues() == null || componentConfiguration.getFromQueues().isEmpty())
				throw new RequiredInputMissingException("Missing required queues to retrieve content from");
			for(String fromQueueId : componentConfiguration.getFromQueues()) {
				if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(fromQueueId))))
					throw new RequiredInputMissingException("Unknown source queue '"+fromQueueId+"' referenced");
			}
			
		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: EMITTER
		} else if(componentConfiguration.getType() == MicroPipelineComponentType.EMITTER) {

			if(componentConfiguration.getFromQueues() == null || componentConfiguration.getFromQueues().isEmpty())
				throw new RequiredInputMissingException("Missing required queues to retrieve content from");
			for(String fromQueueId : componentConfiguration.getFromQueues()) {
				if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(fromQueueId))))
					throw new RequiredInputMissingException("Unknown source queue '"+fromQueueId+"' referenced");
			}
		}
		//
		////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////
		// instantiate component class
		try {
			return this.componentRepository.newInstance(componentConfiguration.getId(), componentConfiguration.getName(), componentConfiguration.getVersion(), componentConfiguration.getSettings());
		} catch(Exception e) {
			throw new ComponentInitializationFailedException("Failed to initialize component '"+componentConfiguration.getId()+"'. Error: " + e.getMessage());
		}
		//
		////////////////////////////////////////////////////////////////////////////////////

	}

//	/**
//	 * Step through {@link Collection} of {@link StreamingMessageQueue} instances and attempts to 
//	 * shut them down. Any exceptions thrown when calling {@link StreamingMessageQueue#shutdown()} 
//	 * are just logged.
//	 * @param queues
//	 * @deprecated moved to {@link MicroPipeline#shutdown()}
//	 */
//	protected void forceQueueShutdown(final Collection<StreamingMessageQueue> queues) {
//		if(queues != null && !queues.isEmpty()) {
//			for(StreamingMessageQueue queue : queues) {
//				try {
//					queue.shutdown();					
//					if(logger.isDebugEnabled())
//						logger.debug("forced queue shutdown [id="+queue.getId()+"]");
//				} catch(Exception e) {
//					logger.error("Forced queue shutdown failed [id="+queue.getId()+"]. Reason: " + e.getMessage());
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Step through {@link Collection] of {@link MicroPipelineComponent} instances and attempts to
//	 * shut them down. Any exceptions thrown when calling {@link MicroPipelineComponent#shutdown()}
//	 * are just logged.
//	 * @param components
//	 * @deprecated moved to {@link MicroPipeline#shutdown()}
//	 */
//	protected void forceComponentShutdown(final Collection<MicroPipelineComponent> components) {
//		if(components != null && !components.isEmpty()) {
//			for(MicroPipelineComponent component : components) {
//				try {
//					component.shutdown();
//					
//					if(logger.isDebugEnabled())
//						logger.debug("forced component shutdown [id="+component.getId()+"]");
//				} catch(Exception e) {
//					logger.error("Forced component shutdown failed [id="+component.getId()+"]. Reason: " + e.getMessage());
//				}
//			}
//		}
//	}
}
