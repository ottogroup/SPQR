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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConfiguration;

/**
 * Validates provided {@link MicroPipelineConfiguration} for being compliant with requirement which 
 * ensure that
 * <ul>
 *   <li>configuration must not be null</li>
 *   <li>set of queues to interconnect components must not be empty</li>
 *   <li>assigned queue identifiers must be unique within pipeline boundaries</li>
 *   <li>set of components must not be empty</li>
 *   <li>assigned component identifiers must be unique within pipeline boundaries</li>
 *   <li>each component configuration must show a valid type</li>
 *   <li>each component configuration must show a <i>name</i> and <i>version</i> which both reference existing artifacts</li>
 *   <li>each component must point to a valid <i>in-queue</i> - unless the component is of type {@link MicroPipelineComponentType#SOURCE}</li>
 *   <li>each component must point to a valid <i>out-queue</i> - unless the component is of type {@link MicroPipelineComponentType#EMITTER}</li>
 * </ul>
 * @author mnxfst
 * @since Apr 13, 2015
 */
public class MicroPipelineValidator {

	
	/**
	 * Validates the contents of a provided {@link MicroPipelineConfiguration} for being compliant with a required format
	 * and errors that may be inferred from provided contents
	 * @param configuration
	 * @return
	 */
	public MicroPipelineValidationResult validate(final MicroPipelineConfiguration configuration) {

		///////////////////////////////////////////////////////////////////////////////////
		// validate configuration, components and queues for not being null 
		if(configuration == null)
			return MicroPipelineValidationResult.MISSING_CONFIGURATION;
		if(configuration.getComponents() == null || configuration.getComponents().isEmpty())
			return MicroPipelineValidationResult.MISSING_COMPONENTS;
		if(configuration.getQueues() == null || configuration.getQueues().isEmpty())
			return MicroPipelineValidationResult.MISSING_QUEUES;
		//
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// validate queues and store their identifiers in set for further look-ups executed
		// on component evaluation
		Set<String> queueIdentifiers = new HashSet<>();
		for(final StreamingMessageQueueConfiguration queueCfg : configuration.getQueues()) {

			// queue identifier must neither be null nor empty
			if(StringUtils.isBlank(queueCfg.getId())) 
				return MicroPipelineValidationResult.MISSING_QUEUE_ID;

			// convert to trimmed lower-case representation and check if it is unique
			String tempId = StringUtils.lowerCase(StringUtils.trim(queueCfg.getId()));
			if(queueIdentifiers.contains(tempId))
				return MicroPipelineValidationResult.NON_UNIQUE_QUEUE_ID;
			queueIdentifiers.add(tempId);
		}		
		//
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// validate components
		Set<String> componentIdentifiers = new HashSet<>();
		for(final MicroPipelineComponentConfiguration componentCfg : configuration.getComponents()) {

			MicroPipelineValidationResult componentValidationResult = validateComponent(componentCfg, queueIdentifiers, componentIdentifiers);
			if(componentValidationResult != MicroPipelineValidationResult.OK)
				return componentValidationResult;

			// add identifier to set of managed components
			componentIdentifiers.add(StringUtils.lowerCase(StringUtils.trim(componentCfg.getId())));
		}		
		//
		///////////////////////////////////////////////////////////////////////////////////
		
		// no errors found so far which could be inferred from configuration 
		return MicroPipelineValidationResult.OK;
		
	}
	
	/**
	 * Validates the provided {@link StreamingMessageQueueConfiguration} for being compliant with basic requirements
	 * set for queues inside {@link MicroPipelineConfiguration}
	 * @param queueCfg
	 * @param queueIdentifiers
	 * @return
	 */
	protected MicroPipelineValidationResult validateQueue(final StreamingMessageQueueConfiguration queueCfg, final Set<String> queueIdentifiers) {
		
		// the queue configuration must not be null ... for obvious reasons ;-)
		if(queueCfg == null)
			return MicroPipelineValidationResult.MISSING_QUEUE_CONFIGURATION;
		
		// queue identifier must neither be null nor empty
		if(StringUtils.isBlank(queueCfg.getId())) 
			return MicroPipelineValidationResult.MISSING_QUEUE_ID;

		// convert to trimmed lower-case representation and check if it is unique
		String tempId = StringUtils.lowerCase(StringUtils.trim(queueCfg.getId()));
		if(queueIdentifiers.contains(tempId))
			return MicroPipelineValidationResult.NON_UNIQUE_QUEUE_ID;
		
		return MicroPipelineValidationResult.OK;
	}
	
	/**
	 * Validates a single {@link MicroPipelineComponentConfiguration} 
	 * @param componentCfg
	 * @param queueIdentifiers previously extracted queue identifiers
	 * @param componentIdentifiers previously extracted component identifiers
	 * @return
	 */
	protected MicroPipelineValidationResult validateComponent(final MicroPipelineComponentConfiguration componentCfg, final Set<String> queueIdentifiers, final Set<String> componentIdentifiers) {
		
		///////////////////////////////////////////////////////////////////////////////////
		// component and its id, name, version and type for neither being null empty
		if(componentCfg == null)
			return MicroPipelineValidationResult.MISSING_COMPONENT_CONFIGURATION;
		if(StringUtils.isBlank(componentCfg.getId()))
			return MicroPipelineValidationResult.MISSING_COMPONENT_ID;
		if(StringUtils.isBlank(componentCfg.getName()))
			return MicroPipelineValidationResult.MISSING_COMPONENT_NAME;
		if(StringUtils.isBlank(componentCfg.getVersion()))
			return MicroPipelineValidationResult.MISSING_COMPONENT_VERSION;
		if(componentCfg.getType() == null)
			return MicroPipelineValidationResult.MISSING_COMPONENT_TYPE;
		//
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// convert id to trimmed lower-case representation and check if it is unique
		String tempId = StringUtils.lowerCase(StringUtils.trim(componentCfg.getId()));
		if(componentIdentifiers.contains(tempId))
			return MicroPipelineValidationResult.NON_UNIQUE_COMPONENT_ID;
		//
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// validate queue settings for specific component types
		if(componentCfg.getType() == MicroPipelineComponentType.SOURCE) {

			// source component must reference a destination queue only - source queue references are not permitted  
			if(StringUtils.isNotBlank(componentCfg.getFromQueue()))
				return MicroPipelineValidationResult.NOT_PERMITTED_SOURCE_QUEUE_REF;
			
			// the identifier of the destination queue must neither be null nor empty
			if(StringUtils.isBlank(componentCfg.getToQueue()))
				return MicroPipelineValidationResult.MISSING_DESTINATION_QUEUE;
			
			// the identifier of the destination queue must reference an existing queue
			String destinationQueueId = StringUtils.lowerCase(StringUtils.trim(componentCfg.getToQueue()));
			if(!queueIdentifiers.contains(destinationQueueId))
				return MicroPipelineValidationResult.UNKNOWN_DESTINATION_QUEUE;
		} else if(componentCfg.getType() == MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR || 
				  componentCfg.getType() == MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR) {

			// operators must reference source and destination queues alike - both variable values must not be empty
			if(StringUtils.isBlank(componentCfg.getFromQueue()))
				return MicroPipelineValidationResult.MISSING_SOURCE_QUEUE;
			if(StringUtils.isBlank(componentCfg.getToQueue()))
				return MicroPipelineValidationResult.MISSING_DESTINATION_QUEUE;

			// the identifier of the source queue must reference an existing queue
			String sourceQueueId = StringUtils.lowerCase(StringUtils.trim(componentCfg.getFromQueue()));
			if(!queueIdentifiers.contains(sourceQueueId))
				return MicroPipelineValidationResult.UNKNOWN_SOURCE_QUEUE;				
			
			// the identifier of the destination queue must reference an existing queue
			String destinationQueueId = StringUtils.lowerCase(StringUtils.trim(componentCfg.getToQueue()));
			if(!queueIdentifiers.contains(destinationQueueId))
				return MicroPipelineValidationResult.UNKNOWN_DESTINATION_QUEUE;
		} else if(componentCfg.getType() == MicroPipelineComponentType.EMITTER) {

			// emitter component must reference a source queue only - destination queue references are not permitted
			if(StringUtils.isNotBlank(componentCfg.getToQueue()))
				return MicroPipelineValidationResult.NOT_PERMITTED_DESTINATION_QUEUE_REF;

			// the identifier of the source queue must neither be null nor empty
			if(StringUtils.isBlank(componentCfg.getFromQueue()))
				return MicroPipelineValidationResult.MISSING_SOURCE_QUEUE;
			
			// the identifier of the destination queue must reference an existing queue
			String destinationQueueId = StringUtils.lowerCase(StringUtils.trim(componentCfg.getFromQueue()));
			if(!queueIdentifiers.contains(destinationQueueId))
				return MicroPipelineValidationResult.UNKNOWN_SOURCE_QUEUE;
		}

		// return true if no error could be derived from component configuration
		return MicroPipelineValidationResult.OK;
	}
	
	
}
