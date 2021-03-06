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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information collected by the {@link SPQRNodeSupervisor} when checking the current
 * state of a spqr processing node
 * @author mnxfst
 * @since Apr 13, 2015
 */
public class SPQRNodeSupervisorResult implements Serializable {

	private static final long serialVersionUID = 8402125222047766899L;

	/** unique node identifier previously assigned by node manager */
	@JsonProperty(value="nodeId", required=true)
	private String nodeId = null;
	
	
}
