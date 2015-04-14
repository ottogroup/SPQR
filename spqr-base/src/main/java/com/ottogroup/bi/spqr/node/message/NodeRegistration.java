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
package com.ottogroup.bi.spqr.node.message;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * All necessary information required for processing node registration
 * @author mnxfst
 * @since Apr 13, 2015
 */
public class NodeRegistration implements Serializable {

	private static final long serialVersionUID = 7705558777452312428L;

	public enum NodeRegistrationState implements Serializable {
		OK, MISSING_PROTOCOL, MISSING_HOST, MISSING_SERVICE_PORT, MISSING_ADMIN_PORT, MISSING_REQUEST, NODE_ID_COMPUTATION_FAILED, TECHNICAL_ERROR
	}
	
	@JsonRootName(value="nodeRegistrationRequest")
	public static class NodeRegistrationRequest implements Serializable {
		
		private static final long serialVersionUID = 666656242856426666L;

		/** protocol used to communicate, http, https ... */
		@JsonProperty(value="protocol", required=true)
		private String protocol = null;
		/** host the node to register lives on */
		@JsonProperty(value="host",required=true)
		private String host = null;
		/** port used to publish the service api (pipeline instantiation, shutdown, stats) */
		@JsonProperty(value="servicePort", required=true)
		private int servicePort = 9090;
		/** port to manage the server itself (watchdog service, thread monitoring) */
		@JsonProperty(value="adminPort", required=true)
		private int adminPort = 9090;
		
		public NodeRegistrationRequest() {			
		}
		
		public NodeRegistrationRequest(final String protocol, final String host, final int servicePort, final int adminPort) {
			this.protocol = protocol;
			this.host = host;
			this.servicePort = servicePort;
			this.adminPort = adminPort;
		}

		public String getProtocol() {
			return protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getServicePort() {
			return servicePort;
		}

		public void setServicePort(int servicePort) {
			this.servicePort = servicePort;
		}

		public int getAdminPort() {
			return adminPort;
		}

		public void setAdminPort(int adminPort) {
			this.adminPort = adminPort;
		}	
	}
	
	public static class NodeRegistrationResponse implements Serializable {

		private static final long serialVersionUID = -2087050771465701123L;

		/** state of registration request */
		@JsonProperty(value="state", required=true)
		private NodeRegistrationState state = NodeRegistrationState.OK;
		
		/** assigned identifier */
		@JsonProperty(value="id", required=true)
		private String id = null;
		
		/** optional message */
		@JsonProperty(value="msg", required=true)
		private String message = null;
		
		public NodeRegistrationResponse() {			
		}
		
		public NodeRegistrationResponse(final String id, final NodeRegistrationState state) {
			this.id = id;
			this.state = state;
		}

		public NodeRegistrationResponse(final String id, final NodeRegistrationState state, final String msg) {
			this.id = id;
			this.state = state;
			this.message = msg;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public NodeRegistrationState getState() {
			return state;
		}

		public void setState(NodeRegistrationState state) {
			this.state = state;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}		
	}
	
}
