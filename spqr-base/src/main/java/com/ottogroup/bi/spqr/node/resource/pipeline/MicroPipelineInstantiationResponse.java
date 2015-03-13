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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class MicroPipelineInstantiationResponse implements Serializable {

	private static final long serialVersionUID = -4416878440036933504L;

	public enum MicroPipelineInstantationState implements Serializable {		
		OK, CONFIGURATION_MISSING, NON_UNIQUE_PIPELINE_ID, QUEUE_INITIALIZATION_FAILED, COMPONENT_INITIALIZATION_FAILED, PIPELINE_INITIALIZATION_FAILED		
	}
	
	@JsonProperty(value="state", required=true)
	private MicroPipelineInstantationState state = MicroPipelineInstantationState.OK;
	@JsonProperty(value="msg", required=true)
	private String message = null;
	@JsonProperty(value="pid", required=true)
	private String pipelineId = null;
	
	public MicroPipelineInstantiationResponse() {		
	}
	
	public MicroPipelineInstantiationResponse(final String pipelineId, final MicroPipelineInstantationState state, final String message) {
		this.pipelineId = pipelineId;
		this.state = state;
		this.message = message;
	}

	public MicroPipelineInstantationState getState() {
		return state;
	}

	public void setState(MicroPipelineInstantationState state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
	}
	
		
}
