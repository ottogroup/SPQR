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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConfiguration;

/**
 * Test case for {@link MicroPipelineValidator}
 * @author mnxfst
 * @since Apr 13, 2015
 */
public class MicroPipelineValidatorTest {

	/**
	 * Test case for {@link MicroPipelineValidator#validate(MicroPipelineConfiguration)} being provided null as input
	 */
	@Test
	public void testValidate_withNullInput() {
		Assert.assertEquals("Required configuration missing", MicroPipelineValidationResult.MISSING_CONFIGURATION, new MicroPipelineValidator().validate(null));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validate(MicroPipelineConfiguration)} being provided an empty components list
	 */
	@Test
	public void testValidate_withEmptyComponentsList() {
		@SuppressWarnings("unchecked")
		List<StreamingMessageQueueConfiguration> mockQueueCfg = Mockito.mock(ArrayList.class);
		Mockito.when(mockQueueCfg.isEmpty()).thenReturn(false);
		
		MicroPipelineConfiguration mockCfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(mockCfg.getQueues()).thenReturn(mockQueueCfg);
		Mockito.when(mockCfg.getComponents()).thenReturn(null);
		
		Assert.assertEquals("Components missing", MicroPipelineValidationResult.MISSING_COMPONENTS, new MicroPipelineValidator().validate(mockCfg));
		
		Mockito.verify(mockCfg, Mockito.times(1)).getComponents();		
		Mockito.verify(mockCfg, Mockito.never()).getQueues();
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validate(MicroPipelineConfiguration)} being provided an empty queues list
	 */
	@Test
	public void testValidate_withEmptyQueuesList() {
		@SuppressWarnings("unchecked")
		List<MicroPipelineComponentConfiguration> mockComponentCfg = Mockito.mock(ArrayList.class);
		Mockito.when(mockComponentCfg.isEmpty()).thenReturn(false);
		
		MicroPipelineConfiguration mockCfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(mockCfg.getComponents()).thenReturn(mockComponentCfg);
		Mockito.when(mockCfg.getQueues()).thenReturn(null);
		
		Assert.assertEquals("Queues missing", MicroPipelineValidationResult.MISSING_QUEUES, new MicroPipelineValidator().validate(mockCfg));
		
		Mockito.verify(mockComponentCfg).isEmpty();
		Mockito.verify(mockCfg, Mockito.times(2)).getComponents();
		Mockito.verify(mockCfg, Mockito.times(1)).getQueues();
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validate(MicroPipelineConfiguration)} being provided a valid configuration
	 */
	@Test
	public void testValidate_withValidConfiguration() {
		
		List<StreamingMessageQueueConfiguration> queues = new ArrayList<StreamingMessageQueueConfiguration>();
		queues.add(new StreamingMessageQueueConfiguration("src-to-operator"));
		queues.add(new StreamingMessageQueueConfiguration("operator-to-emitter"));
		
		List<MicroPipelineComponentConfiguration> components = new ArrayList<MicroPipelineComponentConfiguration>();
		MicroPipelineComponentConfiguration srcCfg = new MicroPipelineComponentConfiguration();
		srcCfg.setFromQueue("");
		srcCfg.setId("test-source-id");
		srcCfg.setName("test-source-name");
		srcCfg.setSettings(null);
		srcCfg.setToQueue("src-to-operator");
		srcCfg.setType(MicroPipelineComponentType.SOURCE);
		srcCfg.setVersion("test-source-version");
		components.add(srcCfg);

		MicroPipelineComponentConfiguration operatorCfg = new MicroPipelineComponentConfiguration();
		operatorCfg.setFromQueue("src-to-operator");
		operatorCfg.setId("test-operator-id");
		operatorCfg.setName("test-operator-name");
		operatorCfg.setSettings(null);
		operatorCfg.setToQueue("operator-to-emitter");
		operatorCfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		operatorCfg.setVersion("test-operator-version");
		components.add(operatorCfg);

		MicroPipelineComponentConfiguration emitterCfg = new MicroPipelineComponentConfiguration();
		emitterCfg.setFromQueue("operator-to-emitter");
		emitterCfg.setId("test-emitter-id");
		emitterCfg.setName("test-emitter-name");
		emitterCfg.setSettings(null);
		emitterCfg.setToQueue("");
		emitterCfg.setType(MicroPipelineComponentType.EMITTER);
		emitterCfg.setVersion("test-emitter-version");
		components.add(emitterCfg);
		
		MicroPipelineConfiguration configuration = new MicroPipelineConfiguration();
		configuration.setComponents(components);
		configuration.setQueues(queues);		
		
		Assert.assertEquals("Valid configuration, no errors expected", MicroPipelineValidationResult.OK, new MicroPipelineValidator().validate(configuration));
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// test cases for validateQueue
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateQueue(StreamingMessageQueueConfiguration, Set)} being provided null as input
	 */
	@Test
	public void testValidateQueue_withNullInput() {
		Assert.assertEquals("Missing queue configuration", MicroPipelineValidationResult.MISSING_QUEUE_CONFIGURATION, 
				new MicroPipelineValidator().validateQueue(null, Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateQueue(StreamingMessageQueueConfiguration, Set)} being provided a 
	 * configuration missing the identifier
	 */
	@Test
	public void testValidateQueue_withMissingQueueId() {
		Assert.assertEquals("Missing queue configuration", MicroPipelineValidationResult.MISSING_QUEUE_ID, 
				new MicroPipelineValidator().validateQueue(new StreamingMessageQueueConfiguration(""), Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateQueue(StreamingMessageQueueConfiguration, Set)} being provided a 
	 * configuration with non-unique id
	 */
	@Test
	public void testValidateQueue_withDuplicateQueueId() {
		Set<String> queueIdentifiers = new HashSet<String>();
		queueIdentifiers.add("test-id");
		Assert.assertEquals("Missing queue configuration", MicroPipelineValidationResult.NON_UNIQUE_QUEUE_ID, 
				new MicroPipelineValidator().validateQueue(new StreamingMessageQueueConfiguration("test-id"), queueIdentifiers));
	}
	
	// end: test cases for validateQueue
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// test cases for validateComponent
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided null input 
	 */
	@Test
	public void testValidateComponent_withNullInput() {
		Assert.assertEquals("Missing component configuration", MicroPipelineValidationResult.MISSING_COMPONENT_CONFIGURATION, 
				new MicroPipelineValidator().validateComponent(null, Collections.<String>emptySet(), Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration missing the identifier 
	 */
	@Test
	public void testValidateComponent_withMissingComponentId() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Missing component id", MicroPipelineValidationResult.MISSING_COMPONENT_ID,  
				new MicroPipelineValidator().validateComponent(cfg, queues, Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration missing the name 
	 */
	@Test
	public void testValidateComponent_withMissingComponentName() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-id");
		cfg.setName("");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Missing component name", MicroPipelineValidationResult.MISSING_COMPONENT_NAME,  
				new MicroPipelineValidator().validateComponent(cfg, queues, Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration missing the version 
	 */
	@Test
	public void testValidateComponent_withMissingComponentVersion() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-id");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR);
		cfg.setVersion("");
		
		Assert.assertEquals("Missing component version", MicroPipelineValidationResult.MISSING_COMPONENT_VERSION,  
				new MicroPipelineValidator().validateComponent(cfg, queues, Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration missing the type
	 */
	@Test
	public void testValidateComponent_withMissingComponentType() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-id");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(null);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Missing component type", MicroPipelineValidationResult.MISSING_COMPONENT_TYPE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, Collections.<String>emptySet()));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration with non-unique id
	 */
	@Test
	public void testValidateComponent_withDuplicateComponentId() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		components.add("test-component");
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Duplicate component ids are not permitted", MicroPipelineValidationResult.NON_UNIQUE_COMPONENT_ID,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a source which has a source queue reference
	 */
	@Test
	public void testValidateComponent_withSourceReferencingSourceQueue() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.SOURCE);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Source queues not permitted for source components", MicroPipelineValidationResult.NOT_PERMITTED_SOURCE_QUEUE_REF,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a source missing the destination queue
	 */
	@Test
	public void testValidateComponent_withSourceMissingDestinationQueue() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("");
		cfg.setType(MicroPipelineComponentType.SOURCE);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Missing destination queue", MicroPipelineValidationResult.MISSING_DESTINATION_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}

	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a source referencing an unknnown destination queue
	 */
	@Test
	public void testValidateComponent_withSourceUnknownDestinationQueue() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("unknown-queue");
		cfg.setType(MicroPipelineComponentType.SOURCE);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.UNKNOWN_DESTINATION_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a valid source 
	 */
	@Test
	public void testValidateComponent_withValidSource() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.SOURCE);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.OK,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an operator missing the source queue reference 
	 */
	@Test
	public void testValidateComponent_withOperatorMissingSourceQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.MISSING_SOURCE_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an operator missing the destination queue reference 
	 */
	@Test
	public void testValidateComponent_withOperatorMissingDestinationQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("");
		cfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.MISSING_DESTINATION_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an operator referencing an unknown source queue 
	 */
	@Test
	public void testValidateComponent_withOperatorUnknownSourceQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("unknown-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.UNKNOWN_SOURCE_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}

	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an operator referencing an unknown destination queue 
	 */
	@Test
	public void testValidateComponent_withOperatorUnknownDestinationQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("unknown-output");
		cfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.UNKNOWN_DESTINATION_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a valid operator 
	 */
	@Test
	public void testValidateComponent_withValidOperator() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.OK,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an emitter referencing a destination queue 
	 */
	@Test
	public void testValidateComponent_withEmitterDestinationQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("test-output");
		cfg.setType(MicroPipelineComponentType.EMITTER);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.NOT_PERMITTED_DESTINATION_QUEUE_REF,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an emitter missing the source queue ref 
	 */
	@Test
	public void testValidateComponent_withEmitterMissingSourceQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("");
		cfg.setType(MicroPipelineComponentType.EMITTER);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.MISSING_SOURCE_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing an emitter referencing an unknown source queue 
	 */
	@Test
	public void testValidateComponent_withEmitterUnknownSourceQueueRef() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("unknown-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("");
		cfg.setType(MicroPipelineComponentType.EMITTER);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.UNKNOWN_SOURCE_QUEUE,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	/**
	 * Test case for {@link MicroPipelineValidator#validateComponent(MicroPipelineComponentConfiguration, java.util.Set, java.util.Set)} being
	 * provided a configuration showing a valid emitter 
	 */
	@Test
	public void testValidateComponent_withValidEmitter() {
		
		Set<String> queues = new HashSet<>();
		queues.add("test-input");
		queues.add("test-output");
		
		Set<String> components = new HashSet<String>();
		
		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setFromQueue("test-input");
		cfg.setId("test-component");
		cfg.setName("test-name");
		cfg.setSettings(new Properties());
		cfg.setToQueue("");
		cfg.setType(MicroPipelineComponentType.EMITTER);
		cfg.setVersion("test-version");
		
		Assert.assertEquals("Unknown destination queue", MicroPipelineValidationResult.OK,  
				new MicroPipelineValidator().validateComponent(cfg, queues, components));
	}
	
	// end: test cases for validateComponent
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
