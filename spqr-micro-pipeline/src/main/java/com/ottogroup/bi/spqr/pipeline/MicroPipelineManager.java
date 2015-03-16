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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.NonUniqueIdentifierException;
import com.ottogroup.bi.spqr.exception.PipelineInstantiationFailedException;
import com.ottogroup.bi.spqr.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.repository.ComponentRepository;

/**
 * Manages the instantiation and monitoring of {@link MicroPipeline micro pipelines} 
 * @author mnxfst
 * @since Mar 13, 2015
 * TODO using one execution service may cause troubles, need to check on that but for the moment having only one suffice
 */
public class MicroPipelineManager {

	private static final Logger logger = Logger.getLogger(MicroPipelineManager.class);
	
	/** keeps track of all registered pipeline instances */
	private final Map<String, MicroPipeline> pipelines = new HashMap<>();
	/** reference towards execution service which is to be used for runtime environment execution */
	private final ExecutorService executorService;
	/** micro pipeline factory */
	private final MicroPipelineFactory microPipelineFactory;
	
	/**
	 * Initializes the micro pipeline manager
	 * @param componentRepository reference to {@link ComponentRepository} which provides access to all {@link MicroPipelineComponent}
	 * @param maxNumberOfThreads max. number of threads assigned to {@link ExecutorService} (1 = single threaded, n = fixed number of threads, other = cached thread pool)
	 * @throws RequiredInputMissingException   
	 */
	public MicroPipelineManager(final ComponentRepository componentRepository, final int maxNumberOfThreads) throws RequiredInputMissingException {

		//////////////////////////////////////////////////////////////////////////////
		// validate provided input
		if(componentRepository == null)
			throw new RequiredInputMissingException("Missing required component repository");
		//
		//////////////////////////////////////////////////////////////////////////////

		this.microPipelineFactory = new MicroPipelineFactory(componentRepository);
		
		if(maxNumberOfThreads == 1)
			this.executorService = Executors.newSingleThreadExecutor();
		else if(maxNumberOfThreads > 1)
			this.executorService = Executors.newFixedThreadPool(maxNumberOfThreads);
		else
			this.executorService = Executors.newCachedThreadPool();
		
	}
	
	/**
	 * Initializes the micro pipeline manager - <b>used for testing purpose only</b>
	 * @param factory
	 * @param executorService
	 * @throws RequiredInputMissingException   
	 */
	protected MicroPipelineManager(final MicroPipelineFactory factory, final ExecutorService executorService) throws RequiredInputMissingException {

		//////////////////////////////////////////////////////////////////////////////
		// validate provided input
		if(factory == null)
			throw new RequiredInputMissingException("Missing required component repository");
		if(executorService == null)
			throw new RequiredInputMissingException("Missing required executor service");
		//
		//////////////////////////////////////////////////////////////////////////////

		this.microPipelineFactory = factory;
		this.executorService = executorService;
	}
	
	/**
	 * Instantiates and executes the {@link MicroPipeline} described by the given {@link MicroPipelineConfiguration}
	 * @param configuration
	 * @throws RequiredInputMissingException
	 * @throws QueueInitializationFailedException
	 * @throws ComponentInitializationFailedException
	 * @throws PipelineInstantiationFailedException
	 */
	public String executePipeline(final MicroPipelineConfiguration configuration) throws RequiredInputMissingException, 
		QueueInitializationFailedException, ComponentInitializationFailedException, PipelineInstantiationFailedException, NonUniqueIdentifierException {
		
		///////////////////////////////////////////////////////////
		// validate input
		if(configuration == null)
			throw new RequiredInputMissingException("Missing required pipeline configuration");
		//
		///////////////////////////////////////////////////////////
		
		String id = StringUtils.lowerCase(StringUtils.trim(configuration.getId()));
		if(this.pipelines.containsKey(id))
			throw new NonUniqueIdentifierException("A pipeline already exists for id '"+id+"'");
		
		
		
		MicroPipeline pipeline = this.microPipelineFactory.instantiatePipeline(configuration, this.executorService);		
		if(pipeline != null) 
			this.pipelines.put(id, pipeline);
		else
			throw new PipelineInstantiationFailedException("Failed to instantiate pipeline '"+configuration.getId()+"'. Reason: null returned by pipeline factory");
		
		if(logger.isDebugEnabled())
			logger.debug("pipeline registered[id="+configuration.getId()+"]");
		
		return id;
	}
	
	/**
	 * Shuts down the referenced pipeline
	 * @param pipelineId
	 */
	public String shutdownPipeline(final String pipelineId) {
		
		String id = StringUtils.lowerCase(StringUtils.trim(pipelineId));
		MicroPipeline pipeline = this.pipelines.get(id);
		if(pipeline != null) {
			pipeline.shutdown();
			this.pipelines.remove(id);
			
			if(logger.isDebugEnabled())
				logger.debug("pipeline shutdown[id="+pipelineId+"]");			
		}
		
		return pipelineId;
	}
	
	/**
	 * Returns the number of registered {@link MicroPipeline} instances
	 * @return
	 */
	public int getNumOfRegisteredPipelines() {
		return this.pipelines.size();
	}
	
}
