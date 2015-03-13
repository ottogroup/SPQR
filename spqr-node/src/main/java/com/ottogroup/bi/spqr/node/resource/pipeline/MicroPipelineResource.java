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
package com.ottogroup.bi.spqr.node.resource.pipeline;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineInstantiationResponse.MicroPipelineInstantationState;
import com.ottogroup.bi.spqr.pipeline.MicroPipeline;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineManager;
import com.ottogroup.bi.spqr.pipeline.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.NonUniqueIdentifierException;
import com.ottogroup.bi.spqr.pipeline.exception.PipelineInstantiationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.QueueInitializationFailedException;

/**
 * REST resource providing access to {@link MicroPipeline pipelines} managed by {@link MicroPipelineManager}
 * @author mnxfst
 * @since Mar 13, 2015
 */
@Path("/pipelines")
public class MicroPipelineResource {

	private final MicroPipelineManager microPipelineManager;
	
	/**
	 * Initializes the micro pipeline resource using the provided input
	 * @param microPipelineManager
	 */
	public MicroPipelineResource(final MicroPipelineManager microPipelineManager) {
		this.microPipelineManager = microPipelineManager;
	}
	
	/**
	 * Creates a new pipeline for the given identifier and {@link MicroPipelineConfiguration}
	 * @param pipelineId
	 * @param configuration
	 * @return
	 */
	@Produces(value = "application/json")
	@POST
	@Path("{pipelineId}")
	public MicroPipelineInstantiationResponse instantiatePipeline(@PathParam("pipelineId") final String pipelineId, final MicroPipelineConfiguration configuration) {		
		try {
			String id = this.microPipelineManager.executePipeline(configuration);
			return new MicroPipelineInstantiationResponse(id, MicroPipelineInstantationState.OK, "");
		} catch (RequiredInputMissingException e) {
			return new MicroPipelineInstantiationResponse(pipelineId, MicroPipelineInstantationState.CONFIGURATION_MISSING, e.getMessage());
		} catch (QueueInitializationFailedException e) {
			return new MicroPipelineInstantiationResponse(pipelineId, MicroPipelineInstantationState.QUEUE_INITIALIZATION_FAILED, e.getMessage());
		} catch (ComponentInitializationFailedException e) {
			return new MicroPipelineInstantiationResponse(pipelineId, MicroPipelineInstantationState.COMPONENT_INITIALIZATION_FAILED, e.getMessage());
		} catch (PipelineInstantiationFailedException e) {
			return new MicroPipelineInstantiationResponse(pipelineId, MicroPipelineInstantationState.PIPELINE_INITIALIZATION_FAILED, e.getMessage());
		} catch (NonUniqueIdentifierException e) {
			return new MicroPipelineInstantiationResponse(pipelineId, MicroPipelineInstantationState.NON_UNIQUE_PIPELINE_ID, e.getMessage());
		}		
	}
	
}
