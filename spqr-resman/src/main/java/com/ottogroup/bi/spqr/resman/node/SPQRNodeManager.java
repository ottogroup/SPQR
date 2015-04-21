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
package com.ottogroup.bi.spqr.resman.node;

import io.dropwizard.client.JerseyClientBuilder;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationState;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;

/**
 * Keeps track of all processing nodes which registered themselves with the SQPR cluster. It provides direct
 * access to each node through dedicated {@link SPQRNodeClient} instances. Aside it manages a set of 
 * {@link SPQRNodeSupervisor supervisors} which look after a single node, retrieve statistical data and 
 * ensures that managed nodes are still alive.  
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class SPQRNodeManager {
	
	/** UUID generator based on ethernet address of the server this application is executed on */
	private final TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
	/** keeps track of all registered spqr nodes */
	private final Map<String, SPQRNodeClient> processingNodes = new HashMap<>();
	/** number of retries to compute an unique node identifier */
	private final int numIdentifierComputationRetries;
	/** builder to use for creating new rest client */
	private final JerseyClientBuilder httpClientBuilder;
	
	
	/**
	 * Initializes the manager using the provided input
	 * @param numIdentifierComputationRetries
	 * @param httpClientBuilder
	 */
	public SPQRNodeManager(final int numIdentifierComputationRetries, final JerseyClientBuilder httpClientBuilder) {
		this.numIdentifierComputationRetries = numIdentifierComputationRetries;
		this.httpClientBuilder = httpClientBuilder;
	}
	
	/**
	 * Registers a new processing node living at the given host and reachable at the provided ports (admin and service).  
	 * @param remoteHost
	 * @param servicePort
	 * @param adminPort
	 * @return
	 */
	public NodeRegistrationResponse registerNode(final String protocol, final String remoteHost, final int servicePort, final int adminPort) {
		
		if(StringUtils.isBlank(protocol))
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_PROTOCOL, "Missing required protocol");
		if(StringUtils.isBlank(remoteHost))
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_HOST, "Missing required remote host");
		if(servicePort < 1)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_SERVICE_PORT, "Missing required service port");
		if(adminPort < 1)
			return new NodeRegistrationResponse("", NodeRegistrationState.MISSING_ADMIN_PORT, "Missing required admin port");

		///////////////////////////////////////////////////////////////////////////////
		// compute identifier assigned to node
		String nodeId = null;
		int retries = this.numIdentifierComputationRetries;
		while(retries > 0) {
			nodeId = uuidGenerator.generate().toString();
			if(!this.processingNodes.containsKey(nodeId))
				break;
		}
		if(nodeId == null || this.processingNodes.containsKey(nodeId))
			return new NodeRegistrationResponse("", NodeRegistrationState.NODE_ID_COMPUTATION_FAILED, "Failed to compute node identifier");
		//
		///////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////
		// register client to access remote node if required
		try {
			this.processingNodes.put(nodeId, new SPQRNodeClient(protocol, remoteHost, servicePort, adminPort, getHttpClient(nodeId)));
		} catch(RequiredInputMissingException e) {
			return new NodeRegistrationResponse("", NodeRegistrationState.TECHNICAL_ERROR, "Failed to create spqr node client. Error: " +e.getMessage());
		}
		//
		///////////////////////////////////////////////////////////////////////////////
		return new NodeRegistrationResponse(nodeId, NodeRegistrationState.OK, "");		
	}
	
	/**
	 * De-registers the referenced node 
	 * @param nodeId
	 * @return
	 */
	public NodeDeRegistrationResponse deregisterNode(final String nodeId) {
		
		/////////////////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(nodeId))
			return new NodeDeRegistrationResponse("", NodeDeRegistrationState.MISSING_NODE_ID, "");
		String id = StringUtils.lowerCase(StringUtils.trim(nodeId));
		if(!this.processingNodes.containsKey(id))
			return new NodeDeRegistrationResponse(nodeId, NodeDeRegistrationState.NO_SUCH_NODE_ID, "Unknown node id: " + nodeId);
		//
		/////////////////////////////////////////////////////////////////////////////

		final SPQRNodeClient client = this.processingNodes.remove(id);
		if(client != null)
			client.shutdown();

		return new NodeDeRegistrationResponse(nodeId, NodeDeRegistrationState.OK, "");
	}
	
	/**
	 * Returns true in case the reference node is registered with this manager instance
	 * @param nodeId
	 * @return
	 */
	public boolean hasNode(final String nodeId) {
		return this.processingNodes.containsKey(StringUtils.lowerCase(StringUtils.trim(nodeId)));
	}
	
	/**
	 * Creates a new {@link Client} instance and assigns the given identifier
	 * @param clientId
	 * @return
	 */
	protected Client getHttpClient(final String clientId) {
		return httpClientBuilder.build(clientId);
	}

}
