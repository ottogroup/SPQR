/**
 * Copyright 2014 Otto (GmbH & Co KG)
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
package com.ottogroup.bi.spqr.repository;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;

/**
 * Asap component description as extracted from classes annotated as {@link AsapComponent}
 * @author mnxfst
 * @since Oct 29, 2014
 *
 */
public class ComponentDescriptor implements Serializable {

	private static final long serialVersionUID = -1649046223771601278L;

	@JsonProperty ( value = "componentClass", required = true )
	private String componentClass = null;
	@JsonProperty ( value = "type", required = true )
	private MicroPipelineComponentType type = null;
	@JsonProperty ( value = "name", required = true )
	private String name = null;
	@JsonProperty ( value = "version", required = true )
	private String version = null;
	@JsonProperty ( value = "description", required = true )
	private String description = null;
	
	/**
	 * Default constructor
	 */
	public ComponentDescriptor() {		
	}
	
	/**
	 * Initializes the descriptor using the provided input
	 * @param componentClass
	 * @param type
	 * @param name
	 * @param version
	 * @param description
	 */
	public ComponentDescriptor(final String componentClass, final MicroPipelineComponentType type, final String name, final String version, final String description) {
		this.componentClass = componentClass;
		this.type = type;
		this.name = name;
		this.version = version;
		this.description = description;
	}
	

	public String getComponentClass() {
		return componentClass;
	}

	public void setComponentClass(String componentClass) {
		this.componentClass = componentClass;
	}

	public MicroPipelineComponentType getType() {
		return type;
	}

	public void setType(MicroPipelineComponentType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
