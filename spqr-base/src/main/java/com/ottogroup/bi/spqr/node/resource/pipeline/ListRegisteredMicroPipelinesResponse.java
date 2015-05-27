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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration;

/**
 * Lists the identifiers of all registered micro pipelines on the given processing node
 * @author mnxfst
 * @since May 27, 2015
 */
@JsonRootName(value="registeredMicroPipelinesResponse")
public class ListRegisteredMicroPipelinesResponse implements Serializable {

	private static final long serialVersionUID = -8492445888382782367L;

	@JsonProperty(value="node", required=true)
	private String node = null;
	@JsonProperty(value="pids", required=true)
	private Set<String> pipelineIds = null;
	@JsonProperty(value="cfgs", required=true) 
	private Map<String, MicroPipelineConfiguration> configurations = new HashMap<>();
	
	public ListRegisteredMicroPipelinesResponse() {		
	}
	
	public ListRegisteredMicroPipelinesResponse(final String node, final Set<String> pipelineIds) {
		this.pipelineIds = pipelineIds;
		this.node = node;
	}

	public ListRegisteredMicroPipelinesResponse(final String node, final Set<String> pipelineIds, final Map<String, MicroPipelineConfiguration> configurations) {
		this.pipelineIds = pipelineIds;
		this.node = node;
		this.configurations = configurations;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Set<String> getPipelineIds() {
		return pipelineIds;
	}

	public void setPipelineIds(Set<String> pipelineIds) {
		this.pipelineIds = pipelineIds;
	}

	public Map<String, MicroPipelineConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(
			Map<String, MicroPipelineConfiguration> configurations) {
		this.configurations = configurations;
	}
	
	
}
