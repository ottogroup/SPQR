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
package com.ottogroup.bi.spqr.resman.resource.pipeline;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineInstantiationResponse;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineValidationResult;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineValidator;


/**
 * Provides a REST API for managing pipelines within the SPQR cluster. It provides methods for
 * <ul>
 *   <li>pipeline instantiation</li>
 *   <li>pipeline shutdown</li>
 *   <li>fetching pipeline statistics</li>
 * </ul>
 * @author mnxfst
 * @since Apr 13, 2015
 */
@Path("/pipelines")
public class SPQRPipelineManagementResource {

	
	/**
	 * Registers a new processing node with the resource manager. It accepts a {@link MicroPipelineConfiguration} which 
	 * is distributed among cluster nodes.
	 */
	@Produces(value = "application/json")
	@Timed(name = "pipeline-instantiation")
	@POST
	public MicroPipelineInstantiationResponse instantiatePipeline(final MicroPipelineConfiguration configuration) {
		
		//////////////////////////////////////////////////////////////////////////
		// validate provided configuration
//		MicroPipelineValidationResult cfgValidationResult = this.pipelineConfigurationValidator.validate(configuration);
//		if(cfgValidationResult != MicroPipelineValidationResult.OK)
//			return new MicroPipelineInstantiationResponse("", cfgValidationResult, "Validation of provided configuration failed");
		//
		//////////////////////////////////////////////////////////////////////////
		
		return null;
				
	}
	
	
}
