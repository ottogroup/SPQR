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
package com.ottogroup.bi.spqr.node.resman;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RemoteClientConnectionFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;

/**
 * Client to manage all communication with the SPQR resource manager
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class SPQRResourceManagerClient {

	/** our faithful logging service .... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRResourceManagerClient.class);
	
	/** client to be used for accessing remote processing node */
	private final Client restClient;
	/** remote service base url */
	private final String resourceManagerServiceBaseUrl;

	/**
	 * Initializes the resource manager client using the provided input
	 * @param resourceManagerProtocol
	 * @param resourceManagerRemoteHost
	 * @param resourceManagerServicePort
	 * @param resourceManagerClient
	 * @throws RequiredInputMissingException
	 */
	public SPQRResourceManagerClient(final String resourceManagerProtocol, final String resourceManagerRemoteHost, 
			final int resourceManagerServicePort, final Client resourceManagerClient) throws RequiredInputMissingException {

		
		///////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(resourceManagerProtocol))
			throw new RequiredInputMissingException("Missing required protocol");
		if(StringUtils.isBlank(resourceManagerRemoteHost))
			throw new RequiredInputMissingException("Missing required remote host");
		if(resourceManagerServicePort < 1)
			throw new RequiredInputMissingException("Missing required service port");
		if(resourceManagerClient == null)
			throw new RequiredInputMissingException("Missing required client");
		//
		///////////////////////////////////////////////////////////////

		this.resourceManagerServiceBaseUrl = new StringBuffer(resourceManagerProtocol).append("://").append(resourceManagerRemoteHost).append(":").append(resourceManagerServicePort).toString();
		this.restClient = resourceManagerClient;		 
	}
	
	/**
	 * Registers this node with the resource manager using the given information as instructions how to access this node
	 * @param nodeProtocol
	 * @param nodeHost
	 * @param nodeServicePort
	 * @param nodeAdminPort
	 * @return
	 */
	public NodeRegistrationResponse registerNode(final String nodeProtocol, final String nodeHost, final int nodeServicePort, final int nodeAdminPort) 
			throws RequiredInputMissingException, RemoteClientConnectionFailedException, IOException {
		
		//////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(nodeProtocol))
			throw new RequiredInputMissingException("Missing required protocol");
		if(StringUtils.isBlank(nodeHost))
			throw new RequiredInputMissingException("Missing required host");
		if(nodeServicePort < 1)
			throw new RequiredInputMissingException("Missing valid service port");
		if(nodeAdminPort < 1)
			throw new RequiredInputMissingException("Missing valid admin port");
		//
		//////////////////////////////////////////////////////////////////
		
		NodeRegistrationRequest message = new NodeRegistrationRequest(nodeProtocol, nodeHost, nodeServicePort, nodeAdminPort);
		StringBuffer url = new StringBuffer(this.resourceManagerServiceBaseUrl).append("/nodes");
		
		if(logger.isDebugEnabled()) 
			logger.debug("Registering processing node [protocol="+nodeProtocol+", host="+nodeHost+", servicePort="+nodeServicePort+", adminPort="+nodeAdminPort+"] at resource manager " + url.toString());
		
		try {
			final WebTarget webTarget = this.restClient.target(url.toString());			
			return webTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(Entity.entity(message, MediaType.APPLICATION_JSON), NodeRegistrationResponse.class);
		} catch(Exception e) {
			throw new RemoteClientConnectionFailedException("Failed to establish a connection with the remote resource manager [url="+url.toString()+"]. Error: " + e.getMessage());
		}
	}
	
	/**
	 * De-Registers the referenced node from the resource manager
	 * @param nodeId
	 * @return
	 * @throws RequiredInputMissingException
	 */
	public NodeDeRegistrationResponse deregisterNode(final String nodeId) throws RequiredInputMissingException, RemoteClientConnectionFailedException {

		//////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(nodeId))
			throw new RequiredInputMissingException("Missing required node identifier");
		//
		//////////////////////////////////////////////////////////////////

		StringBuffer url = new StringBuffer(this.resourceManagerServiceBaseUrl).append("/nodes/").append(nodeId);
		
		if(logger.isDebugEnabled()) 
			logger.debug("De-registering processing node [nodeId="+nodeId+"] at resource manager " + url.toString());
		
		try {
			final WebTarget webTarget = this.restClient.target(url.toString());
			return webTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).delete(NodeDeRegistrationResponse.class);
		} catch(Exception e) {
			throw new RemoteClientConnectionFailedException("Failed to establish a connection with the remote resource manager [url="+url.toString()+"]. Error: " + e.getMessage());
		}
		
	}
	
	
}
