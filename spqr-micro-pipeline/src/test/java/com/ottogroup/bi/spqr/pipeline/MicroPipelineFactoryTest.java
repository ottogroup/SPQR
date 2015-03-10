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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConfiguration;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;
import com.ottogroup.bi.spqr.repository.ComponentRepository;
import com.ottogroup.bi.spqr.repository.exception.ComponentInstantiationFailedException;
import com.ottogroup.bi.spqr.repository.exception.UnknownComponentException;

/**
 * Test case for {@link MicroPipelineFactory}
 * @author mnxfst
 * @since Mar 6, 2015
 */
public class MicroPipelineFactoryTest {

	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * null as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInstantiatePipeline_withNullInput() throws QueueInitializationFailedException, ComponentInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * an empty configuration as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInstantiatePipeline_withEmptyInput() throws QueueInitializationFailedException, ComponentInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(new MicroPipelineConfiguration());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * an empty identifier as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInstantiatePipeline_withEmptyId() throws QueueInitializationFailedException, ComponentInitializationFailedException {
		
		MicroPipelineComponentConfiguration componentCfg = Mockito.mock(MicroPipelineComponentConfiguration.class);
		StreamingMessageQueueConfiguration queueCfg = Mockito.mock(StreamingMessageQueueConfiguration.class);
		
		try {
			MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
			cfg.setId("");
			cfg.getComponents().add(componentCfg);
			cfg.getQueues().add(queueCfg);
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(cfg);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * an empty component cfg list as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInstantiatePipeline_withEmptyComponentCfg() throws QueueInitializationFailedException, ComponentInitializationFailedException {
		
		StreamingMessageQueueConfiguration queueCfg = Mockito.mock(StreamingMessageQueueConfiguration.class);
		
		try {
			MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
			cfg.setId("test");
			cfg.getQueues().add(queueCfg);
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(cfg);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * an empty queue cfg list as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInstantiatePipeline_withEmptyQueueCfg() throws QueueInitializationFailedException, ComponentInitializationFailedException {
		
		MicroPipelineComponentConfiguration componentCfg = Mockito.mock(MicroPipelineComponentConfiguration.class);
		
		try {
			MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
			cfg.setId("test");
			cfg.getComponents().add(componentCfg);
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(cfg);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * a queue cfg list which holds two configurations referenced by the same id which must lead to a {@link QueueInitializationFailedException}
	 */
	@Test
	public void testInstantiatePipeline_withNonUniqueQueueId() throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		MicroPipelineComponentConfiguration componentCfg = Mockito.mock(MicroPipelineComponentConfiguration.class);
		
		Properties queueProps = new Properties();

		StreamingMessageQueueConfiguration validQueueCfg = new StreamingMessageQueueConfiguration();
		validQueueCfg.setId("testInstantiatePipeline_withNonUniqueQueueId");
		validQueueCfg.setProperties(queueProps);
		
		StreamingMessageQueueConfiguration invalidQueueCfg = new StreamingMessageQueueConfiguration();
		invalidQueueCfg.setId("testInstantiatePipeline_withNonUniqueQueueId");
		invalidQueueCfg.setProperties(queueProps);
		
		try {
			MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
			cfg.setId("test");
			cfg.getComponents().add(componentCfg);
			cfg.getQueues().add(validQueueCfg);
			cfg.getQueues().add(invalidQueueCfg);
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).instantiatePipeline(cfg);
			Assert.fail("Missing required input");
		} catch(QueueInitializationFailedException e) {
			// expected
		}
	}	

	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * a component cfg which references an unknown component which must lead to a {@link ComponentInitializationFailedException}
	 */
	@Test
	public void testInstantiatePipeline_withInvalidComponentConfiguration() throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInstantiationFailedException, UnknownComponentException {
		
		StreamingMessageQueueConfiguration validQueueCfg = new StreamingMessageQueueConfiguration();
		validQueueCfg.setId("queue-1");
		
		MicroPipelineComponentConfiguration component1Cfg = new MicroPipelineComponentConfiguration();
		component1Cfg.setId("test-id");			
		component1Cfg.setSettings(new Properties());
		component1Cfg.setType(MicroPipelineComponentType.SOURCE);
		component1Cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue");
		component1Cfg.setVersion("0.0.1");
		component1Cfg.getToQueues().add("queue-1");
		
		MicroPipelineComponentConfiguration component2Cfg = new MicroPipelineComponentConfiguration();
		component2Cfg.setId("test-id-2");			
		component2Cfg.setSettings(new Properties());
		component2Cfg.setType(MicroPipelineComponentType.SOURCE);
		component2Cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue-2");
		component2Cfg.setVersion("0.0.1");
		component2Cfg.getToQueues().add("queue-1");
		
		MicroPipelineConfiguration pipelineCfg = new MicroPipelineConfiguration();
		pipelineCfg.setId("test-id");
		pipelineCfg.getQueues().add(validQueueCfg);
		pipelineCfg.getComponents().add(component2Cfg);
		pipelineCfg.getComponents().add(component1Cfg);
		
		MicroPipelineComponent mockComponent2 = Mockito.mock(MicroPipelineComponent.class);
		Mockito.when(mockComponent2.getId()).thenReturn("mock-component-2");
		
		ComponentRepository repo = Mockito.mock(ComponentRepository.class);
		Mockito.when(repo.newInstance(component1Cfg.getId(), component1Cfg.getName(), component1Cfg.getVersion(), component1Cfg.getSettings())).thenThrow(new UnknownComponentException("Unknown component class"));
		Mockito.when(repo.newInstance(component2Cfg.getId(), component2Cfg.getName(), component2Cfg.getVersion(), component2Cfg.getSettings())).thenReturn(mockComponent2);
		
		try {
			new MicroPipelineFactory(repo).instantiatePipeline(pipelineCfg);
			Assert.fail("Missing required input");
		} catch(ComponentInitializationFailedException e) {
			// expected
		}
	}	

	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * a cfg which references the same component twice which must lead to a {@link ComponentInitializationFailedException}
	 */
	@Test
	public void testInstantiatePipeline_withComponentConfigurationTwice() throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInstantiationFailedException, UnknownComponentException {
		
		StreamingMessageQueueConfiguration validQueueCfg = new StreamingMessageQueueConfiguration();
		validQueueCfg.setId("queue-1");
		
		MicroPipelineComponentConfiguration component1Cfg = new MicroPipelineComponentConfiguration();
		component1Cfg.setId("test-id");			
		component1Cfg.setSettings(new Properties());
		component1Cfg.setType(MicroPipelineComponentType.SOURCE);
		component1Cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue");
		component1Cfg.setVersion("0.0.1");
		component1Cfg.getToQueues().add("queue-1");
		
		MicroPipelineConfiguration pipelineCfg = new MicroPipelineConfiguration();
		pipelineCfg.setId("test-id");
		pipelineCfg.getQueues().add(validQueueCfg);
		pipelineCfg.getComponents().add(component1Cfg);
		pipelineCfg.getComponents().add(component1Cfg);
		
		MicroPipelineComponent mockComponent1 = Mockito.mock(MicroPipelineComponent.class);
		Mockito.when(mockComponent1.getId()).thenReturn("mock-component-1");
		Mockito.when(mockComponent1.getType()).thenReturn(MicroPipelineComponentType.SOURCE);
		
		ComponentRepository repo = Mockito.mock(ComponentRepository.class);
		Mockito.when(repo.newInstance(component1Cfg.getId(), component1Cfg.getName(), component1Cfg.getVersion(), component1Cfg.getSettings())).thenReturn(mockComponent1);
		
		try {
			new MicroPipelineFactory(repo).instantiatePipeline(pipelineCfg);
			Assert.fail("Missing required input");
		} catch(ComponentInitializationFailedException e) {
			// expected
		}
	}	
	

	/**
	 * Test case for {@link MicroPipelineFactory#instantiatePipeline(MicroPipelineConfiguration)} being provided
	 * a cfg which references a component with type null which must lead to a {@link ComponentInitializationFailedException}
	 */
	@Test
	public void testInstantiatePipeline_withComponentTypeNull() throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInstantiationFailedException, UnknownComponentException {
		
		StreamingMessageQueueConfiguration validQueueCfg = new StreamingMessageQueueConfiguration();
		validQueueCfg.setId("queue-1");
		
		MicroPipelineComponentConfiguration component1Cfg = new MicroPipelineComponentConfiguration();
		component1Cfg.setId("test-id");			
		component1Cfg.setSettings(new Properties());
		component1Cfg.setType(MicroPipelineComponentType.SOURCE);
		component1Cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue");
		component1Cfg.setVersion("0.0.1");
		component1Cfg.getToQueues().add("queue-1");
		
		MicroPipelineConfiguration pipelineCfg = new MicroPipelineConfiguration();
		pipelineCfg.setId("test-id");
		pipelineCfg.getQueues().add(validQueueCfg);
		pipelineCfg.getComponents().add(component1Cfg);
		
		MicroPipelineComponent mockComponent1 = Mockito.mock(MicroPipelineComponent.class);
		Mockito.when(mockComponent1.getId()).thenReturn("mock-component-1");
		Mockito.when(mockComponent1.getType()).thenReturn(null);
		
		ComponentRepository repo = Mockito.mock(ComponentRepository.class);
		Mockito.when(repo.newInstance(component1Cfg.getId(), component1Cfg.getName(), component1Cfg.getVersion(), component1Cfg.getSettings())).thenReturn(mockComponent1);
		
		try {
			new MicroPipelineFactory(repo).instantiatePipeline(pipelineCfg);
			Assert.fail("Missing required input");
		} catch(ComponentInitializationFailedException e) {
			// expected
		}
	}	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// @see MicroPipelineFactory#initializeQueue
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeQueue(StreamingMessageQueueConfiguration)} being
	 * provided null as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitializeQueue_withNullInput() throws QueueInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeQueue(null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeQueue(StreamingMessageQueueConfiguration)} being
	 * provided an empty configuration as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitializeQueue_withEmptyInput() throws QueueInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeQueue(new StreamingMessageQueueConfiguration());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeQueue(StreamingMessageQueueConfiguration)} being
	 * provided an empty queue identifier as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitializeQueue_withEmptyQueueId() throws QueueInitializationFailedException {
		try {
			StreamingMessageQueueConfiguration cfg = new StreamingMessageQueueConfiguration();
			cfg.setProperties(new Properties());
			cfg.setId("");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeQueue(cfg);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	/**
	 * Test case for {@link MicroPipelineFactory#initializeQueue(StreamingMessageQueueConfiguration)} being
	 * provided null as queue settings as input which must be processed without problems
	 */
	@Test
	public void testInitializeQueue_withNullQueueSettings() throws QueueInitializationFailedException, RequiredInputMissingException {
		StreamingMessageQueueConfiguration cfg = new StreamingMessageQueueConfiguration();
		cfg.setProperties(null);
		cfg.setId("test-id");
		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeQueue(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeQueue(StreamingMessageQueueConfiguration)} being
	 * provided valid input to receive {@link DefaultStreamingMessageQueue}.
	 */
	@Test
	public void testInitializeQueue_withValidSettingsForDefaultQueue() throws RequiredInputMissingException, QueueInitializationFailedException {
		Properties props = new Properties();
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));
		StreamingMessageQueueConfiguration cfg = new StreamingMessageQueueConfiguration();
		cfg.setProperties(props);
		cfg.setId("testInitializeQueue_withValidSettingsForDefaultQueue");
		StreamingMessageQueue queue = new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeQueue(cfg);
		Assert.assertNotNull("The queue instance must not be null");;
		Assert.assertEquals("The classes must be equal", DefaultStreamingMessageQueue.class, queue.getClass());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// @see MicroPipelineFactory#initializeComponent

	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * null as input which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withNullInput() throws ComponentInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(null, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withEmptyInput() throws ComponentInitializationFailedException {
		try {
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(new MicroPipelineComponentConfiguration(), Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input to component type which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withEmptyComponentType() throws ComponentInitializationFailedException {
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");;
			cfg.setSettings(new Properties());
			cfg.setName("");
			cfg.setVersion("0.0.1");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input to component class which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withEmptyComponentClass() throws ComponentInitializationFailedException {
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");;
			cfg.setSettings(new Properties());
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("");
			cfg.setVersion("0.0.1");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input to component version which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withEmptyComponentVersion() throws ComponentInitializationFailedException {
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");;
			cfg.setSettings(new Properties());
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("test-class");
			cfg.setVersion("");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input to component settings which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_withEmptyComponentSettings() throws ComponentInitializationFailedException {
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");;
			cfg.setSettings(null);
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("test-name");
			cfg.setVersion("0.0.1");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * an empty input to queues to write to which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_source_withEmptyToQueuesSettings() throws ComponentInitializationFailedException {
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");			
			cfg.setSettings(new Properties());
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("test name");
			cfg.setVersion("0.0.1");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * a set of two queues to write to which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testInitializeComponent_source_with2ToQueues() throws ComponentInitializationFailedException {
		Map<String, StreamingMessageQueue> queues = Mockito.mock(HashMap.class);
		Mockito.when(queues.containsKey("queue-1")).thenReturn(true);
		Mockito.when(queues.containsKey("queue-2")).thenReturn(true);

		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");			
			cfg.setSettings(new Properties());
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("test name");
			cfg.setVersion("0.0.1");
			cfg.getToQueues().add("queue-1");
			cfg.getToQueues().add("queue-2");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
		Mockito.verify(queues, Mockito.never()).containsKey("queue-1");
		Mockito.verify(queues, Mockito.never()).containsKey("queue-2");
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * a reference towards a queue that does not exist which must lead to a {@link RequiredInputMissingException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_source_withReferenceToUnknownQueue() throws ComponentInitializationFailedException {
		
		try {
			MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
			cfg.setId("test-id");			
			cfg.setSettings(new Properties());
			cfg.setType(MicroPipelineComponentType.SOURCE);
			cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue");
			cfg.setVersion("0.0.1");
			cfg.getToQueues().add("unknown-queue");
			new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).initializeComponent(cfg, Collections.<String, StreamingMessageQueue>emptyMap());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineFactory#initializeComponent(MicroPipelineComponentConfiguration)} being provided
	 * a valid configuration for a {@link MicroPipelineComponentType#SOURCE} component. When accessing the repository 
	 * it throws an exception which must lead to a {@link ComponentInitializationFailedException}
	 * @throws ComponentInitializationFailedException
	 */
	@Test
	public void testInitializeComponent_source_withValidConfigurationButExceptionThrownByRepository() throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, StreamingMessageQueue> queues = Mockito.mock(HashMap.class);
		Mockito.when(queues.containsKey("queue-1")).thenReturn(true);

		MicroPipelineComponentConfiguration cfg = new MicroPipelineComponentConfiguration();
		cfg.setId("test-id");			
		cfg.setSettings(new Properties());
		cfg.setType(MicroPipelineComponentType.SOURCE);
		cfg.setName("testInitializeComponent_source_withReferenceToUnknownQueue");
		cfg.setVersion("0.0.1");
		cfg.getToQueues().add("queue-1");
		
		ComponentRepository repo = Mockito.mock(ComponentRepository.class);
		Mockito.when(repo.newInstance(cfg.getId(), cfg.getName(), cfg.getVersion(), cfg.getSettings())).thenThrow(new NullPointerException());

		try {
			new MicroPipelineFactory(repo).initializeComponent(cfg, queues);
			Assert.fail("Repo access fails");
		} catch(ComponentInitializationFailedException e) {
			// expected
		}
		Mockito.verify(queues).containsKey("queue-1");

	}
//
//	//////////////////////////////////////////////////////////////////////////////////////////////////////////
//	// @see MicroPipelineFactory#forceQueueShutdown
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceQueueShutdown(java.util.Collection)} being provided
//	 * null as input which must have no effects 
//	 */
//	@Test
//	public void testForceQueueShutdown_withNullInput() {
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceQueueShutdown(null);
//	}
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceQueueShutdown(java.util.Collection)} being provided
//	 * an empty set as input which must have no effects 
//	 */
//	@Test
//	public void testForceQueueShutdown_withEmptyInput() {
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceQueueShutdown(Collections.<StreamingMessageQueue>emptySet());
//	}
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceQueueShutdown(java.util.Collection)} being provided
//	 * a list having two elements where the first throws a {@link NullPointerException}. That behavior must have
//	 * no impact on method execution - the second queue must be shut down as well.
//	 */
//	@Test
//	public void testForceQueueShutdown_withQueueThrowingNPE() {
//		
//		StreamingMessageQueue normalQueue = Mockito.mock(StreamingMessageQueue.class);
//		StreamingMessageQueue npeQueue = Mockito.mock(StreamingMessageQueue.class);
//		
//		Mockito.when(npeQueue.getId()).thenReturn("testForceQueueShutdown_withQueueThrowingNPE-NPE_QUEUE");
//		Mockito.when(npeQueue.shutdown()).thenThrow(new NullPointerException());
//		
//		Mockito.when(normalQueue.getId()).thenReturn("testForceQueueShutdown_withQueueThrowingNPE-NORMAL_QUEUE");
//		Mockito.when(normalQueue.shutdown()).thenReturn(true);
//		
//		List<StreamingMessageQueue> queues = new ArrayList<>();
//		queues.add(npeQueue);
//		queues.add(normalQueue);
//		
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceQueueShutdown(queues);
//		
//		Mockito.verify(normalQueue).shutdown();
//		Mockito.verify(npeQueue).shutdown();
//		Mockito.verify(npeQueue, Mockito.times(1)).getId();
//	}
//	
//	//////////////////////////////////////////////////////////////////////////////////////////////////////////
//	// @see MicroPipelineFactory#forceComponentShutdown
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceComponentShutdown(java.util.Collection)} being provided
//	 * null as input which must have no effects 
//	 */
//	@Test
//	public void testForceComponentShutdown_withNullInput() {
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceComponentShutdown(null);
//	}
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceComponentShutdown(java.util.Collection)} being provided
//	 * an empty set as input which must have no effects 
//	 */
//	@Test
//	public void testForceComponentShutdown_withEmptyInput() {
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceComponentShutdown(Collections.<MicroPipelineComponent>emptySet());
//	}
//
//	/**
//	 * Test case for {@link MicroPipelineFactory#forceComponentShutdown(java.util.Collection)} being provided
//	 * a list having two elements where the first throws a {@link NullPointerException}. That behavior must have
//	 * no impact on method execution - the second component must be shut down as well.
//	 */
//	@Test
//	public void testForceComponentShutdown_withComponentThrowingNPE() {
//		
//		MicroPipelineComponent normalComponent = Mockito.mock(MicroPipelineComponent.class);
//		MicroPipelineComponent npeComponent = Mockito.mock(MicroPipelineComponent.class);
//		
//		Mockito.when(npeComponent.getId()).thenReturn("testForceComponentShutdown_withComponentThrowingNPE-NPE_COMPONENT");
//		Mockito.when(npeComponent.shutdown()).thenThrow(new NullPointerException());
//		
//		Mockito.when(normalComponent.getId()).thenReturn("testForceComponentShutdown_withComponentThrowingNPE-NORMAL_COMPONENT");
//		Mockito.when(normalComponent.shutdown()).thenReturn(true);
//		
//		List<MicroPipelineComponent> components = new ArrayList<>();
//		components.add(npeComponent);
//		components.add(normalComponent);
//		
//		new MicroPipelineFactory(Mockito.mock(ComponentRepository.class)).forceComponentShutdown(components);
//		
//		Mockito.verify(normalComponent).shutdown();
//		Mockito.verify(npeComponent).shutdown();
//		Mockito.verify(npeComponent, Mockito.times(1)).getId();
//	}
}
