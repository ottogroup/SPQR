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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.NonUniqueIdentifierException;
import com.ottogroup.bi.spqr.exception.PipelineInstantiationFailedException;
import com.ottogroup.bi.spqr.exception.QueueInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineShutdownResponse.MicroPipelineShutdownState;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineManager;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineValidationResult;

/**
 * Test case for {@link MicroPipelineResource}
 * @author mnxfst
 * @since Mar 16, 2015
 */
public class MicroPipelineResourceTest {
	
	/**
	 * Test case for {@link MicroPipelineResource#MicroPipelineResource(com.ottogroup.bi.spqr.pipeline.MicroPipelineManager)}
	 * being provided null which must lead to a {@link RequiredInputMissingException} 
	 */
	@Test
	public void testConstructor_withNullInput() {
		try {
			new MicroPipelineResource(null);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided an empty pipeline id which must lead to {@link MicroPipelineInstantiationResponse} showing {@link MicroPipelineValidationResult#MISSING_CONFIGURATION}
	 * as state
	 */
	@Test	
	public void testInstantiatePipeline_withNullPipelineId() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn(null);
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.MISSING_CONFIGURATION, response.getState());
		Assert.assertEquals("Values must be equal", MicroPipelineResource.ERROR_MSG_PIPELINE_ID_MISSING, response.getMessage());
		Assert.assertTrue("Id must be empty", StringUtils.isBlank(response.getPipelineId()));		
		
		Mockito.verify(microPipelineManager, Mockito.never()).executePipeline(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided an empty pipeline configuration which must lead to {@link MicroPipelineInstantiationResponse} showing {@link MicroPipelineValidationResult#MISSING_CONFIGURATION}
	 * as state
	 */
	@Test	
	public void testInstantiatePipeline_withNullConfiguration() throws Exception {
		
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(null);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.MISSING_CONFIGURATION, response.getState());
		Assert.assertEquals("Values must be equal", MicroPipelineResource.ERROR_MSG_PIPELINE_CONFIGURATION_MISSING, response.getMessage());
		Assert.assertTrue("Value must be empty", StringUtils.isBlank(response.getPipelineId()));
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration missing a required setting which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#MISSING_CONFIGURATION} as state
	 */
	@Test	
	public void testInstantiatePipeline_withPipelineConfigurationMissingSetting() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withPipelineConfigurationMissingSetting");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new RequiredInputMissingException("Missing setting"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.MISSING_CONFIGURATION, response.getState());
		Assert.assertEquals("Values must be equal", "Missing setting", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withPipelineConfigurationMissingSetting", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration that leads to queue init exception which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#QUEUE_INITIALIZATION_FAILED} as state
	 */
	@Test	
	public void testInstantiatePipeline_withQueueInitException() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withQueueInitException");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new QueueInitializationFailedException("Queue init failed"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.QUEUE_INITIALIZATION_FAILED, response.getState());
		Assert.assertEquals("Values must be equal", "Queue init failed", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withQueueInitException", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration that leads to queue component exception which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#COMPONENT_INITIALIZATION_FAILED} as state
	 */
	@Test	
	public void testInstantiatePipeline_withComponentInitException() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withComponentInitException");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new ComponentInitializationFailedException("Component init failed"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.COMPONENT_INITIALIZATION_FAILED, response.getState());
		Assert.assertEquals("Values must be equal", "Component init failed", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withComponentInitException", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration that leads to pipeline init exception which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#PIPELINE_INITIALIZATION_FAILED} as state
	 */
	@Test	
	public void testInstantiatePipeline_withPipelineInitException() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withPipelineInitException");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new PipelineInstantiationFailedException("Pipeline init failed"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.PIPELINE_INITIALIZATION_FAILED, response.getState());
		Assert.assertEquals("Values must be equal", "Pipeline init failed", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withPipelineInitException", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}

	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration that leads to npe which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#TECHNICAL_ERROR} as state
	 */
	@Test	
	public void testInstantiatePipeline_withGeneralException() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withGeneralException");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new NullPointerException("General error"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.TECHNICAL_ERROR, response.getState());
		Assert.assertEquals("Values must be equal", "General error", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withGeneralException", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a pipeline configuration that leads to non-unique id exception which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#NON_UNIQUE_PIPELINE_ID} as state
	 */
	@Test	
	public void testInstantiatePipeline_withNonUniqueIdException() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withNonUniqueIdException");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenThrow(new NonUniqueIdentifierException("Non-unique id"));
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.NON_UNIQUE_PIPELINE_ID, response.getState());
		Assert.assertEquals("Values must be equal", "Non-unique id", response.getMessage());
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withNonUniqueIdException", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#instantiatePipeline(String, com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided a valid pipeline configuration which must lead to {@link MicroPipelineInstantiationResponse} 
	 * showing {@link MicroPipelineValidationResult#OK} as state
	 */
	@Test	
	public void testInstantiatePipeline_withValidConfiguration() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withValidConfiguration");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenReturn("testInstantiatePipeline_withValidConfiguration");
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.OK, response.getState());
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withValidConfiguration", response.getPipelineId());
		
		Mockito.verify(cfg).getId();
		Mockito.verify(microPipelineManager).executePipeline(cfg);
	}

	/**
	 * Test case for {@link MicroPipelineResource#updatePipeline(String, MicroPipelineConfiguration)} being provided the
	 * configuration of an already existing pipeline which must lead to an update. As the code is similar to {@link MicroPipelineResource#instantiatePipeline(String, MicroPipelineConfiguration)}
	 * test cases for invalid input were left out
	 */
	@Test
	public void testUpdatePipeline_withExistingId() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withValidConfiguration");
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenReturn("testInstantiatePipeline_withValidConfiguration");
		Mockito.when(microPipelineManager.hasPipeline(cfg.getId())).thenReturn(true);
		Mockito.when(microPipelineManager.shutdownPipeline(cfg.getId())).thenReturn("testInstantiatePipeline_withValidConfiguration".toLowerCase());
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.OK, response.getState());
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withValidConfiguration", response.getPipelineId());
		
		response = new MicroPipelineResource(microPipelineManager).updatePipeline("testInstantiatePipeline_withValidConfiguration", cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.OK, response.getState());
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withValidConfiguration", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(4)).getId();
		Mockito.verify(microPipelineManager).shutdownPipeline(cfg.getId());
		Mockito.verify(microPipelineManager, Mockito.times(2)).executePipeline(cfg);
		Mockito.verify(microPipelineManager, Mockito.times(1)).hasPipeline("testInstantiatePipeline_withValidConfiguration");
	}

	/**
	 * Test case for {@link MicroPipelineResource#updatePipeline(String, MicroPipelineConfiguration)} being provided a fresh
	 * configuration. As the code is similar to {@link MicroPipelineResource#instantiatePipeline(String, MicroPipelineConfiguration)}
	 * test cases for invalid input were left out
	 */
	@Test
	public void testUpdatePipeline_withNonExistingId() throws Exception {
		
		MicroPipelineConfiguration cfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(cfg.getId()).thenReturn("testInstantiatePipeline_withValidConfiguration");
		
		MicroPipelineConfiguration anotherCfg = Mockito.mock(MicroPipelineConfiguration.class);
		Mockito.when(anotherCfg.getId()).thenReturn("testInstantiatePipeline_withValidConfiguration_new");
		
		MicroPipelineManager microPipelineManager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(microPipelineManager.executePipeline(cfg)).thenReturn("testInstantiatePipeline_withValidConfiguration");
		Mockito.when(microPipelineManager.executePipeline(anotherCfg)).thenReturn("testInstantiatePipeline_withValidConfiguration_new");
		Mockito.when(microPipelineManager.hasPipeline(anotherCfg.getId())).thenReturn(false);
		
		MicroPipelineInstantiationResponse response = new MicroPipelineResource(microPipelineManager).instantiatePipeline(cfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.OK, response.getState());
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withValidConfiguration", response.getPipelineId());
		
		response = new MicroPipelineResource(microPipelineManager).updatePipeline("testInstantiatePipeline_withValidConfiguration_new", anotherCfg);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.OK, response.getState());
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", "testInstantiatePipeline_withValidConfiguration_new", response.getPipelineId());
		
		Mockito.verify(cfg, Mockito.times(1)).getId();
		Mockito.verify(anotherCfg, Mockito.times(2)).getId();
		Mockito.verify(microPipelineManager, Mockito.times(1)).executePipeline(cfg);
		Mockito.verify(microPipelineManager, Mockito.times(1)).executePipeline(anotherCfg);
		Mockito.verify(microPipelineManager, Mockito.times(1)).hasPipeline("testInstantiatePipeline_withValidConfiguration_new");
	}
	
	/**
	 * Test case for {@lin MicroPipelineResource#shutdown(String)} being provided an empty string which
	 * must lead to a {@link MicroPipelineShutdownResponse} showing {@link MicroPipelineShutdownState#PIPELINE_ID_MISSING}
	 */
	@Test
	public void testShutdownPipeline_withEmptyPipelineId() throws Exception {		
		MicroPipelineManager manager = Mockito.mock(MicroPipelineManager.class);
		MicroPipelineShutdownResponse response = new MicroPipelineResource(manager).shutdown("");
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", MicroPipelineResource.ERROR_MSG_PIPELINE_ID_MISSING, response.getMessage());
		Assert.assertEquals("Values must be equal", MicroPipelineShutdownState.PIPELINE_ID_MISSING, response.getState());
		Mockito.verify(manager, Mockito.never()).shutdownPipeline("");
	}
	
	/**
	 * Test case for {@lin MicroPipelineResource#shutdown(String)} where the underlying manager throws a np which
	 * must lead to a {@link MicroPipelineShutdownResponse} showing {@link MicroPipelineShutdownState#TECHNICAL_ERROR}
	 */
	@Test
	public void testShutdownPipeline_withManagerThrowingNPE() throws Exception {		
		MicroPipelineManager manager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(manager.shutdownPipeline("testShutdownPipeline_withManagerThrowingNPE")).thenThrow(new NullPointerException("General error"));
		MicroPipelineShutdownResponse response = new MicroPipelineResource(manager).shutdown("testShutdownPipeline_withManagerThrowingNPE");
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("Values must be equal", "General error", response.getMessage());
		Assert.assertEquals("Values must be equal", MicroPipelineShutdownState.TECHNICAL_ERROR, response.getState());
		Assert.assertEquals("Values must be equal", "testShutdownPipeline_withManagerThrowingNPE", response.getPipelineId());
		Mockito.verify(manager).shutdownPipeline("testShutdownPipeline_withManagerThrowingNPE");
	}
	
	/**
	 * Test case for {@link MicroPipelineResource#shutdown(String)} with valid pipeline id which
	 * must lead to a {@link MicroPipelineShutdownResponse} showing {@link MicroPipelineShutdownState#OK}
	 */
	@Test
	public void testShutdownPipeline_withValidPipelineId() throws Exception {		
		MicroPipelineManager manager = Mockito.mock(MicroPipelineManager.class);
		Mockito.when(manager.shutdownPipeline("testShutdownPipeline_withValidPipelineId")).thenReturn("testShutdownPipeline_withValidPipelineId");
		MicroPipelineShutdownResponse response = new MicroPipelineResource(manager).shutdown("testShutdownPipeline_withValidPipelineId");
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertTrue("Message must be empty", StringUtils.isBlank(response.getMessage()));
		Assert.assertEquals("Values must be equal", MicroPipelineShutdownState.OK, response.getState());
		Assert.assertEquals("Values must be equal", "testShutdownPipeline_withValidPipelineId", response.getPipelineId());
		Mockito.verify(manager).shutdownPipeline("testShutdownPipeline_withValidPipelineId");
	}

}
