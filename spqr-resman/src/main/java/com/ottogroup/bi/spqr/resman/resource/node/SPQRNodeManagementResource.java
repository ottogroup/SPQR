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

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;
import com.ottogroup.bi.spqr.resman.node.SPQRNodeManager;

/**
 * Provides a REST API which allows active processing node registration and shutdown. All nodes that wish to participate in 
 * or leave the cluster formed around and managed by the resource manager must use this API to notify the manager about their existence and availability.
 * @author mnxfst
 * @since Apr 10, 2015
 */
@Path("/nodes")
public class SPQRNodeManagementResource {

	private final SPQRNodeManager nodeManager;
	
	public SPQRNodeManagementResource(final SPQRNodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	/**
	 * Registers the processing node which lives at the location provided inside the request
	 * @param request
	 * @return
	 */
	@Produces(value = "application/json")
	@Timed(name = "node-registration")
	@POST
	public NodeRegistrationResponse registerNode(final NodeRegistrationRequest request) {

		// ensure that the incoming request carries a valid and accessible body
		if(request == null)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_REQUEST, "Missing request body carrying required node information");

		return this.nodeManager.registerNode(request.getProtocol(), request.getHost(), request.getServicePort(), request.getAdminPort());		
	}
	
	/**
	 * De-Registers the referenced processing node and thus removes it from the SQPR cluster
	 * @param nodeId
	 * @return
	 */
	@Produces(value = "application/json")
	@Timed(name = "node-deregistration")
	@Path("{nodeId}")
	@DELETE
	public NodeDeRegistrationResponse deregisterNode(@PathParam("nodeId") final String nodeId) {		
		return this.nodeManager.deregisterNode(nodeId);		
	}
	
}
