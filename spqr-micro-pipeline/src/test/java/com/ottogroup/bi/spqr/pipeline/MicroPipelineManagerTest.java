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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.NonUniqueIdentifierException;
import com.ottogroup.bi.spqr.pipeline.exception.PipelineInstantiationFailedException;
import com.ottogroup.bi.spqr.pipeline.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.repository.ComponentRepository;

/**
 * Test case for {@link MicroPipelineManager}
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class MicroPipelineManagerTest {

	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	
	@AfterClass
	public static void shutdown() {
		if(executorService != null)
			executorService.shutdownNow();
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#MicroPipelineManager(com.ottogroup.bi.spqr.repository.ComponentRepository, int)} being
	 * provided null as input which must lead to a {@link RequiredInputMissingException}
	 */
	@Test
	public void testConstructor_withNullRepository() {
		ComponentRepository repo = null;
		try {
			new MicroPipelineManager(repo, 10);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#executePipeline(MicroPipelineConfiguration)} being provided null as
	 * input which must lead to a  {@link RequiredInputMissingException}
	 */
	@Test
	public void testExecutePipeline_withNullInput() throws RequiredInputMissingException, QueueInitializationFailedException, ComponentInitializationFailedException, PipelineInstantiationFailedException, NonUniqueIdentifierException {		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		try {
			manager.executePipeline(null);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}		
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#executePipeline(MicroPipelineConfiguration)} being provided a
	 * configuration which leads to an exception inside the factory that must be thrown to caller 
	 */
	@Test
	public void testExecutePipeline_withConfigurationLeadingToFactoryException() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withConfigurationLeadingToFactoryException");
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenThrow(new ComponentInitializationFailedException("Failed to initialize component"));
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		try {
			manager.executePipeline(cfg);
			Assert.fail("Invalid input");
		} catch(ComponentInitializationFailedException e) {
			// expected
		}	
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#executePipeline(MicroPipelineConfiguration)} being provided a
	 * configuration which leads to factory returning null what must be thrown to caller as an {@link PipelineInstantiationFailedException}
	 */
	@Test
	public void testExecutePipeline_withConfigurationLeadingFactoryReturnNull() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withConfigurationLeadingFactoryReturnNull");
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenReturn(null);
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		try {
			manager.executePipeline(cfg);
			Assert.fail("Invalid input");
		} catch(PipelineInstantiationFailedException e) {
			// expected
		}		
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);
	}

	/**
	 * Test case for {@link MicroPipelineManager#executePipeline(MicroPipelineConfiguration)} being provided a
	 * valid configuration twice which leads to a {@link NonUniqueIdentifierException}
	 */
	@Test
	public void testExecutePipeline_withValidConfigurationTwice() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withValidConfiguration");
		
		MicroPipeline pipeline = Mockito.mock(MicroPipeline.class);
		Mockito.when(pipeline.getId()).thenReturn(cfg.getId());
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenReturn(pipeline);
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		Assert.assertEquals("Values must be equal", StringUtils.lowerCase(StringUtils.trim(cfg.getId())), manager.executePipeline(cfg));
		Assert.assertEquals("Values must be equal", 1, manager.getNumOfRegisteredPipelines());
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);		
		
		try {
			manager.executePipeline(cfg);
			Assert.fail("A pipeline for that identifier already exists");
		} catch(NonUniqueIdentifierException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#executePipeline(MicroPipelineConfiguration)} being provided a
	 * valid configuration which leads to a registered {@link MicroPipeline} instance
	 */
	@Test
	public void testExecutePipeline_withValidConfiguration() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withValidConfiguration");
		
		MicroPipeline pipeline = Mockito.mock(MicroPipeline.class);
		Mockito.when(pipeline.getId()).thenReturn(cfg.getId());
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenReturn(pipeline);
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		Assert.assertEquals("Values must be equal", StringUtils.lowerCase(StringUtils.trim(cfg.getId())), manager.executePipeline(cfg));
		Assert.assertEquals("Values must be equal", 1, manager.getNumOfRegisteredPipelines());
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);		
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#shutdownPipeline(String)} being provided
	 * null as input which must not change the number of registered pipelines
	 */
	@Test
	public void testShutdownPipeline_withNullInput() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withValidConfiguration");
		
		MicroPipeline pipeline = Mockito.mock(MicroPipeline.class);
		Mockito.when(pipeline.getId()).thenReturn(cfg.getId());
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenReturn(pipeline);
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		Assert.assertEquals("Values must be equal", StringUtils.lowerCase(StringUtils.trim(cfg.getId())), manager.executePipeline(cfg));
		Assert.assertEquals("Values must be equal", 1, manager.getNumOfRegisteredPipelines());
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);		
		
		manager.shutdownPipeline(null);
		Assert.assertEquals("Values must be equal", 1, manager.getNumOfRegisteredPipelines());		
	}
	
	/**
	 * Test case for {@link MicroPipelineManager#shutdownPipeline(String)} being provided
	 * valid id as input which must reduce the number of registered pipelines as well as 
	 * {@link MicroPipeline#shutdown()} must be called
	 */
	@Test
	public void testShutdownPipeline_withValidInput() throws Exception {
		MicroPipelineConfiguration cfg = new MicroPipelineConfiguration();
		cfg.setId("testExecutePipeline_withValidConfiguration");
		
		MicroPipeline pipeline = Mockito.mock(MicroPipeline.class);
		Mockito.when(pipeline.getId()).thenReturn(cfg.getId());
		
		MicroPipelineFactory factory = Mockito.mock(MicroPipelineFactory.class);
		Mockito.when(factory.instantiatePipeline(cfg, executorService)).thenReturn(pipeline);
		
		MicroPipelineManager manager = new MicroPipelineManager(factory, executorService);
		
		Assert.assertEquals("Values must be equal", StringUtils.lowerCase(StringUtils.trim(cfg.getId())), manager.executePipeline(cfg));
		Assert.assertEquals("Values must be equal", 1, manager.getNumOfRegisteredPipelines());
		
		Mockito.verify(factory).instantiatePipeline(cfg, executorService);		
		
		manager.shutdownPipeline(cfg.getId());
		Assert.assertEquals("Values must be equal", 0, manager.getNumOfRegisteredPipelines());
		Mockito.verify(pipeline).shutdown();
	}
	
}
