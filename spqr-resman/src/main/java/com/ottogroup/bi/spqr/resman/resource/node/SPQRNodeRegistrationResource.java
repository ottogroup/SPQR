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
package com.ottogroup.bi.spqr.resman.resource.node;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;

/**
 * Provides a REST API to enable active processing node registration. All nodes that 
 * wish to participate in the cluster formed around and managed by the resource manager
 * must use this API to notify the manager about their existence and availability.
 * @author mnxfst
 * @since Apr 10, 2015
 */
@Path("/nodes")
public class SPQRNodeRegistrationResource {

	/**
	 * Registers a new processing node with the resource manager. It accepts a {@link MicroPipeline
	 */
	@Produces(value = "application/json")
	@Timed(name = "node-registration")
	@POST
	public void registerProcessingNode() {
				
	}
	
}
