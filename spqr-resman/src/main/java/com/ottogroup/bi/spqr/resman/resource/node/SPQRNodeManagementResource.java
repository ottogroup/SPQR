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

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.annotation.Timed;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;

/**
 * Provides a REST API which allows active processing node registration and shutdown. All nodes that wish to participate in 
 * or leave the cluster formed around and managed by the resource manager must use this API to notify the manager about their existence and availability.
 * @author mnxfst
 * @since Apr 10, 2015
 */
@Path("/nodes")
public class SPQRNodeManagementResource {

	/**
	 * required information for node registration: 
	 */

	@Produces(value = "application/json")
	@Timed(name = "node-registration")
	@POST
	public NodeRegistrationResponse registerNode(final NodeRegistrationRequest request) {
		
		///////////////////////////////////////////////////////////////////////////
		// validate request
		if(request == null)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_REQUEST, "Missing request body carrying required node information");
		if(StringUtils.isBlank(request.getHost()))
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_HOST, "Missing required host name for processing node");
		if(request.getServicePort() < 1)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_SERVICE_PORT, "Missing valid service port for processing node");
		if(request.getAdminPort() < 1)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_SERVICE_PORT, "Missing valid admin port for processing node");
		//
		///////////////////////////////////////////////////////////////////////////

		return null;
		
	}
}
