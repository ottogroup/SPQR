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
package com.ottogroup.bi.spqr.pipeline;

import java.io.Serializable;

/**
 * Result states returned by {@link MicroPipelineValidator#validate(MicroPipelineConfiguration)} on config validation
 * @author mnxfst
 * @since Apr 13, 2015
 */
public enum MicroPipelineValidationResult implements Serializable {
	
	// general results
	OK, // configuration is valid (no errors found that could be inferred from configuration
	MISSING_CONFIGURATION, // configuration object is missing at all 
	MISSING_COMPONENTS, // configuration misses component configuration which renders the queue useless
	MISSING_QUEUES, // configuration misses queue configuration and thus communication between components is impossible
	PIPELINE_INITIALIZATION_FAILED, // initialization of the overall pipeline failed for any reason (see optional text message in response if provided)
	QUEUE_INITIALIZATION_FAILED, // initialization of a configured queue failed for any reason (see optional text message in response if provided)  
	COMPONENT_INITIALIZATION_FAILED, // initialization of a configured component failed for any reason (see optional text message in response if provided)
	TECHNICAL_ERROR, // general error ... no categorization possible
	NON_UNIQUE_PIPELINE_ID, // 
	MISSING_PIPELINE_ID, // pipeline identifier missing - mostly generated ... thus: generation error

	// component specific results
	MISSING_COMPONENT_CONFIGURATION, // provided component configuration is empty
	MISSING_COMPONENT_ID, // any of the provided component configurations misses the required identifier 
	NON_UNIQUE_COMPONENT_ID, // any of the provided component configurations shows a non-unique (value is pipeline bounded) identifier
	MISSING_COMPONENT_TYPE,	// any of the provided component configurations misses the required type
	MISSING_COMPONENT_NAME, // any of the provided component configurations misses the required name	
	MISSING_COMPONENT_VERSION, // any of the provided component configurations misses the required version
	UNKNOWN_COMPONENT_NAME, // any of the provided component configurations references an unknown component name
	UNKNOWN_COMPONENT_VERSION, // any of the provided component configurations references an unknown component version
	MISSING_SOURCE_QUEUE, // any of the operator or emitter components shows no source queue
	UNKNOWN_SOURCE_QUEUE, // any of the operator or emitter components reference an unknown source queue
	NOT_PERMITTED_SOURCE_QUEUE_REF, // any of the source components reference a source queue which is not permitted for that type
	MISSING_DESTINATION_QUEUE, // any of the source or operator components shows no destination queue
	UNKNOWN_DESTINATION_QUEUE, // any of the source or operator components reference an unknown destination queue
	NOT_PERMITTED_DESTINATION_QUEUE_REF, // any of the emitter components reference a destination queue which is not permitted for that type
	
	// queue specific results
	MISSING_QUEUE_CONFIGURATION, // provided queue configuration is empty
	MISSING_QUEUE_ID, // any of the provided queue configurations misses the required identifier
	NON_UNIQUE_QUEUE_ID // any of the provided queue configurations shows a non-unique (value is pipeline bounded) identifier	 
}
