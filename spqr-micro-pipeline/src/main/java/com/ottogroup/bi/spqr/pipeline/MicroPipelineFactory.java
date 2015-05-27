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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.metrics.MetricsHandler;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter;
import com.ottogroup.bi.spqr.pipeline.component.emitter.EmitterRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperator;
import com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperatorRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.operator.MessageCountResponseWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.component.operator.OperatorTriggeredWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.component.operator.TimerBasedResponseWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.pipeline.component.source.SourceRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.exception.UnknownWaitStrategyException;
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
	/** identifier of processing node this factory lives on */
	private final String processingNodeId;
	
	
	/**
	 * Initializes the factory using the provided input
	 * @param processingNodeId
	 * @param componentRepository
	 */
	public MicroPipelineFactory(final String processingNodeId, final ComponentRepository componentRepository) {
		this.processingNodeId = processingNodeId;
		this.componentRepository = componentRepository;
	}
	
	
	/**
	 * Instantiates the {@link MicroPipeline} according to the provided {@link MicroPipelineComponentConfiguration} 
	 * @param cfg
	 * @param executorService
	 * @return
	 * @throws RequiredInputMissingException
	 * TODO validate micro pipeline for path from source to emitter
	 */
	public MicroPipeline instantiatePipeline(final MicroPipelineConfiguration cfg, final ExecutorService executorService) throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInitializationFailedException {
		
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

		MetricRegistry.name(StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
				            StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
				            "queue",
				            "messages");
				            
		// TODO deactivate
		final MetricsHandler metricsHandler = new MetricsHandler();
		// TODO START
		metricsHandler.addScheduledReporter("stdout", ConsoleReporter.forRegistry(metricsHandler.getRegistry()).convertDurationsTo(TimeUnit.SECONDS).convertRatesTo(TimeUnit.MILLISECONDS).formattedFor(Locale.GERMANY).build());
		
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
				StreamingMessageQueue queueInstance = initializeQueue(queueConfig);
				
				/////////////////////////////////////////////////////////////////////
				// add queue message insertion and retrieval counters
				// TODO configure
				final Counter queueInsertionCounter = metricsHandler.counter(
						MetricRegistry.name(
								StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
								StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
								"queue",
								id,
								"messages",
								"in"
						), true
				);
				
				final Counter queueRetrievalCounter = metricsHandler.counter(
						MetricRegistry.name(
								StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
								StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
								"queue",
								id,
								"messages",
								"out"
						), true
				);
				queueInstance.setMessageInsertionCounter(queueInsertionCounter);
				queueInstance.setMessageRetrievalCounter(queueRetrievalCounter);
				/////////////////////////////////////////////////////////////////////
				
				microPipeline.addQueue(id, queueInstance);				
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
		boolean sourceComponentFound = false;
		boolean emitterComponentFound = false;
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
				
				final StreamingMessageQueue fromQueue = microPipeline.getQueue(StringUtils.lowerCase(StringUtils.trim(componentCfg.getFromQueue())));
				final StreamingMessageQueue toQueue = microPipeline.getQueue(StringUtils.lowerCase(StringUtils.trim(componentCfg.getToQueue())));
				
				final Counter messageCounter = metricsHandler.counter(
						MetricRegistry.name(
								StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
								StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
								"component",
								id,
								"messages",
								"count"
						), true
				);
				
				switch(component.getType()) {
					case SOURCE: {
						SourceRuntimeEnvironment srcEnv = new SourceRuntimeEnvironment(this.processingNodeId, cfg.getId(), (Source)component, toQueue.getProducer());
						srcEnv.setMessageCounter(messageCounter);
						microPipeline.addSource(id, srcEnv);
						sourceComponentFound = true;
						break;
					}
					case DIRECT_RESPONSE_OPERATOR: {
						
						final Timer messageProcessingTimer = metricsHandler.timer(
								MetricRegistry.name(
										StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
										StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
										"component",
										id,
										"messages",
										"timer"
								)
						);
						DirectResponseOperatorRuntimeEnvironment directResponseEnv = new DirectResponseOperatorRuntimeEnvironment(this.processingNodeId, cfg.getId(), (DirectResponseOperator)component, 
								fromQueue.getConsumer(), toQueue.getProducer());
						directResponseEnv.setMessageCounter(messageCounter);
						directResponseEnv.setMessageProcessingTimer(messageProcessingTimer);
						microPipeline.addOperator(id, directResponseEnv);
						break;
					}
					case DELAYED_RESPONSE_OPERATOR: {
						DelayedResponseOperatorRuntimeEnvironment delayedResponseEnv = new DelayedResponseOperatorRuntimeEnvironment(this.processingNodeId, cfg.getId(), (DelayedResponseOperator)component, getResponseWaitStrategy(componentCfg), 
								fromQueue.getConsumer(), toQueue.getProducer(), executorService);
						delayedResponseEnv.setMessageCounter(messageCounter);
						microPipeline.addOperator(id, delayedResponseEnv);
						break;
					}
					case EMITTER: {
						final Timer messageEmitDurationTimer = metricsHandler.timer(
								MetricRegistry.name(
										StringUtils.lowerCase(StringUtils.trim(this.processingNodeId)),
										StringUtils.lowerCase(StringUtils.trim(cfg.getId())),
										"component",
										id,
										"messages",
										"emit",
										"duration"
								)
						);

						EmitterRuntimeEnvironment emitterEnv = new EmitterRuntimeEnvironment(this.processingNodeId, cfg.getId(), (Emitter)component, fromQueue.getConsumer());
						emitterEnv.setMessageCounter(messageCounter);
						emitterEnv.setMessageEmitDurationTimer(messageEmitDurationTimer);
						microPipeline.addEmitter(id, emitterEnv);
						emitterComponentFound = true;
						break;
					}
				}
				
				components.put(id, component);
			} catch(Exception e) {
				logger.error("component initialization failed [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Forcing shutdown of all queues and components. Reason: " + e.getMessage(), e);
				microPipeline.shutdown();
				throw new ComponentInitializationFailedException("Failed to initialize component [id="+id+", class="+componentCfg.getName()+", version="+componentCfg.getVersion()+"]. Reason: " + e.getMessage(), e);
			}
		}
		
		if(!sourceComponentFound) {
			microPipeline.shutdown();
			throw new RequiredInputMissingException("Missing required source component");
		}
		
		if(!emitterComponentFound) {
			microPipeline.shutdown();
			throw new RequiredInputMissingException("Missing required emitter component");
		}
		
		///////////////////////////////////////////////////////////////////////////////////

		microPipeline.attachComponentMetricsHandler(metricsHandler);
		
		///////////////////////////////////////////////////////////////////////////////////
		// (3) start components --> ramp up their runtime environments 
		for(String sourceId : microPipeline.getSources().keySet()) {
			executorService.submit(microPipeline.getSources().get(sourceId));
			if(logger.isDebugEnabled())
				logger.debug("Started runtime environment for source [id="+sourceId+"]");
		}
		for(String directResponseOperatorId : microPipeline.getDirectResponseOperators().keySet()) {
			executorService.submit(microPipeline.getDirectResponseOperators().get(directResponseOperatorId));
			if(logger.isDebugEnabled())
				logger.debug("Started runtime environment for direct response operator [id="+directResponseOperatorId+"]");
		}
		for(String delayedResponseOperatorId : microPipeline.getDelayedResponseOperators().keySet()) {
			executorService.submit(microPipeline.getDelayedResponseOperators().get(delayedResponseOperatorId));
			if(logger.isDebugEnabled())
				logger.debug("Started runtime environment for delayed response operator [id="+delayedResponseOperatorId+"]");
		}
		for(String emitterId : microPipeline.getEmitters().keySet()) {
			executorService.submit(microPipeline.getEmitters().get(emitterId));
			if(logger.isDebugEnabled())
				logger.debug("Started runtime environment for emitter [id="+emitterId+"]");
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Started stats collector");
		//
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

			if(StringUtils.isBlank(componentConfiguration.getToQueue()))
				throw new RequiredInputMissingException("Missing required queues to write content to");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getToQueue()))))
				throw new RequiredInputMissingException("Unknown destination queue '"+componentConfiguration.getToQueue()+"'");
		 
		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: DIRECT_RESPONSE_OPERATOR
		} else if(componentConfiguration.getType() == MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR) {

			if(StringUtils.isBlank(componentConfiguration.getToQueue()))
				throw new RequiredInputMissingException("Missing required queues to write content to");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getToQueue()))))
				throw new RequiredInputMissingException("Unknown destination queue '"+componentConfiguration.getToQueue()+"'");
			
			if(StringUtils.isBlank(componentConfiguration.getFromQueue()))
				throw new RequiredInputMissingException("Missing required queues to retrieve content from");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getFromQueue()))))
				throw new RequiredInputMissingException("Unknown source queue '"+componentConfiguration.getFromQueue()+"'");

		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: DELAYED_RESPONSE_OPERATOR
		} else if(componentConfiguration.getType() == MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR) {

			if(StringUtils.isBlank(componentConfiguration.getToQueue()))
				throw new RequiredInputMissingException("Missing required queues to write content to");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getToQueue()))))
				throw new RequiredInputMissingException("Unknown destination queue '"+componentConfiguration.getToQueue()+"'");
			
			if(StringUtils.isBlank(componentConfiguration.getFromQueue()))
				throw new RequiredInputMissingException("Missing required queues to retrieve content from");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getFromQueue()))))
				throw new RequiredInputMissingException("Unknown source queue '"+componentConfiguration.getFromQueue()+"'");

			if(StringUtils.isBlank(componentConfiguration.getSettings().getProperty(DelayedResponseOperator.CFG_WAIT_STRATEGY_NAME)))
				throw new RequiredInputMissingException("Missing required settings for wait strategy applied to delayed response operator");

		////////////////////////////////////////////////////////////////////////////////////
		// validate settings for components of type: EMITTER
		} else if(componentConfiguration.getType() == MicroPipelineComponentType.EMITTER) {

			if(StringUtils.isBlank(componentConfiguration.getFromQueue()))
				throw new RequiredInputMissingException("Missing required queues to retrieve content from");
			if(!queues.containsKey(StringUtils.lowerCase(StringUtils.trim(componentConfiguration.getFromQueue()))))
				throw new RequiredInputMissingException("Unknown source queue '"+componentConfiguration.getFromQueue()+"'");
		}
		//
		////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////
		// instantiate component class
		try {
			return this.componentRepository.newInstance(componentConfiguration.getId(), componentConfiguration.getName(), componentConfiguration.getVersion(), componentConfiguration.getSettings());
		} catch(Exception e) {
			throw new ComponentInitializationFailedException("Failed to initialize component '"+componentConfiguration.getId()+"'. Error: " + e.getMessage(), e);
		}
		//
		////////////////////////////////////////////////////////////////////////////////////

	}

	/**
	 * Instantiates, initializes and returns the {@link DelayedResponseOperatorWaitStrategy} configured for the {@link DelayedResponseOperator}
	 * whose {@link MicroPipelineComponentConfiguration configuration} is provided when calling this method. 
	 * @param delayedResponseOperatorCfg
	 * @return
	 */
	protected DelayedResponseOperatorWaitStrategy getResponseWaitStrategy(final MicroPipelineComponentConfiguration delayedResponseOperatorCfg) throws RequiredInputMissingException, UnknownWaitStrategyException {

		/////////////////////////////////////////////////////////////////////////////////////
		// validate input
		if(delayedResponseOperatorCfg == null)
			throw new RequiredInputMissingException("Missing required delayed response operator configuration");
		if(delayedResponseOperatorCfg.getSettings() == null)
			throw new RequiredInputMissingException("Missing required delayed response operator settings");
		String strategyName = StringUtils.lowerCase(StringUtils.trim(delayedResponseOperatorCfg.getSettings().getProperty(DelayedResponseOperator.CFG_WAIT_STRATEGY_NAME)));
		if(StringUtils.isBlank(strategyName)) 
			throw new RequiredInputMissingException("Missing required strategy name expected as part of operator settings ('"+DelayedResponseOperator.CFG_WAIT_STRATEGY_NAME+"')");
		//
		/////////////////////////////////////////////////////////////////////////////////////
		
		if(logger.isDebugEnabled())
			logger.debug("Settings provided for strategy '"+strategyName+"'");
		Properties strategyProperties = new Properties();		
		for(Enumeration<Object> keyEnumerator =  delayedResponseOperatorCfg.getSettings().keys(); keyEnumerator.hasMoreElements();) {
			String key = (String)keyEnumerator.nextElement();
			if(StringUtils.startsWith(key, DelayedResponseOperator.CFG_WAIT_STRATEGY_SETTINGS_PREFIX)) {
				String waitStrategyCfgKey = StringUtils.substring(key, StringUtils.lastIndexOf(key, ".") + 1);
				if(StringUtils.isNoneBlank(waitStrategyCfgKey)) {
					String waitStrategyCfgValue = delayedResponseOperatorCfg.getSettings().getProperty(key);
					strategyProperties.put(waitStrategyCfgKey, waitStrategyCfgValue);
					
					if(logger.isDebugEnabled())
						logger.debug("\t" + waitStrategyCfgKey + ": " + waitStrategyCfgValue);
					
				}
			}			
		}
		
		if(StringUtils.equalsIgnoreCase(strategyName, MessageCountResponseWaitStrategy.WAIT_STRATEGY_NAME)) {
			MessageCountResponseWaitStrategy strategy = new MessageCountResponseWaitStrategy();
			strategy.initialize(strategyProperties);
			return strategy;
		} else if(StringUtils.equalsIgnoreCase(strategyName, TimerBasedResponseWaitStrategy.WAIT_STRATEGY_NAME)) {
			TimerBasedResponseWaitStrategy strategy = new TimerBasedResponseWaitStrategy();
			strategy.initialize(strategyProperties);
			return strategy;
		} else if(StringUtils.equalsIgnoreCase(strategyName, OperatorTriggeredWaitStrategy.WAIT_STRATEGY_NAME)) {
			OperatorTriggeredWaitStrategy strategy = new OperatorTriggeredWaitStrategy();
			strategy.initialize(strategyProperties);
			return strategy;
		}
		
		throw new UnknownWaitStrategyException("Unknown wait strategy '"+strategyName+"'");
		
	}
}
