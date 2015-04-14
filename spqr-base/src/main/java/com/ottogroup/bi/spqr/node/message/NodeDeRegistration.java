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
 * All necessary information required for processing node de-registration
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class NodeDeRegistration implements Serializable {

	private static final long serialVersionUID = 2883144319190044706L;

	public enum NodeDeRegistrationState implements Serializable {
		OK, MISSING_NODE_ID, NO_SUCH_NODE_ID, TECHNICAL_ERROR
	}
	
	/**
	 * Response to de-registration request 
	 * @author mnxfst
	 * @since Apr 14, 2015
	 */
	@JsonRootName(value="nodeDeRegistrationRequest")
	public static class NodeDeRegistrationResponse implements Serializable {
		
		private static final long serialVersionUID = -1108469543678244332L;

		@JsonProperty(value="id", required=true)
		private String id = null;		
		@JsonProperty(value="state", required=true)
		private NodeDeRegistrationState state = NodeDeRegistrationState.OK;
		@JsonProperty(value="message", required=true)
		private String message = null;
		
		public NodeDeRegistrationResponse() {			
		}
		
		public NodeDeRegistrationResponse(final String id, final NodeDeRegistrationState state, final String message) {
			this.id = id;
			this.state = state;
			this.message = message;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public NodeDeRegistrationState getState() {
			return state;
		}

		public void setState(NodeDeRegistrationState state) {
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
