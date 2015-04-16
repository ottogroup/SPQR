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



import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineInstantiationResponse;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineShutdownResponse;
import com.ottogroup.bi.spqr.node.resource.pipeline.MicroPipelineShutdownResponse.MicroPipelineShutdownState;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration;
import com.ottogroup.bi.spqr.pipeline.MicroPipelineValidationResult;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentConfiguration;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConfiguration;

/**
 * Test case for {@link SPQRNodeClient}
 * @author mnxfst
 * @since Apr 13, 2015
 *
 */
public class SPQRNodeClientTest {

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * an empty protocol
	 */
	@Test
	public void testConstructor_withEmptyProtocol() {
		try {
			new SPQRNodeClient("", "localhost", 8080, 9090, Mockito.mock(Client.class));
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * an empty remote host
	 */
	@Test
	public void testConstructor_withEmptyRemoteHost() {
		try {
			new SPQRNodeClient("http", "", 8080, 9090, Mockito.mock(Client.class));
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * an invalid service port
	 */
	@Test
	public void testConstructor_withInvalidServicePort() {
		try {
			new SPQRNodeClient("http", "", -1, 9090, Mockito.mock(Client.class));
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * an invalid admin port
	 */
	@Test
	public void testConstructor_withInvalidAdminPort() {
		try {
			new SPQRNodeClient("http", "", 8080, -1, Mockito.mock(Client.class));
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * null as client ref
	 */
	@Test
	public void testConstructor_withNullClient() {
		try {
			new SPQRNodeClient("http", "", 8080, 8081, null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeClient#SPQRNodeClient(String, String, int, int, com.sun.jersey.api.client.Client)} being provided
	 * valid settings
	 */
	@Test
	public void testConstructor_withValidInput() throws Exception {
		SPQRNodeClient client = new SPQRNodeClient("http", "localhost", 8080, 8081, Mockito.mock(Client.class));
		Assert.assertEquals("Values must be equal", "http://localhost:8080", client.getProcessingNodeServiceBaseUrl());
		Assert.assertEquals("Values must be equal", "http://localhost:8081", client.getProcessingNodeAdminBaseUrl());
	}
	
	/**
	 * Test case for {@link SPQRNodeClient#instantiatePipeline(com.ottogroup.bi.spqr.pipeline.MicroPipelineConfiguration)} being
	 * provided null to test the validation of incoming configurations. As the validator is tested somewhere else it is not
	 * necessary to do all in-depth thorough checking again as the constraints do not change
	 */
	@Test
	public void testInstantiatePipeline_withEmptyConfiguration() throws Exception {
		SPQRNodeClient client = new SPQRNodeClient("http", "localhost", 8080, 8081, Mockito.mock(Client.class));
		Assert.assertEquals("Values must be equal", MicroPipelineValidationResult.MISSING_CONFIGURATION, client.instantiatePipeline(null).getState());
	}

	/**
	 * Test case for {@link SPQRNodeClient#updatePipeline(MicroPipelineConfiguration)}
	 * being provided valid input
	 */
	@Test
	public void testInstantiatePipeline_withValidInput() throws Exception {

		String processingNodeHost = "processing-node-localhost";
		int processingNodeServicePort = 7070;
		int processingNodeAdminPort = 7071;
		MicroPipelineConfiguration sampleConfiguration = new MicroPipelineConfiguration();
		sampleConfiguration.setId("Test");
		
		MicroPipelineComponentConfiguration componentCfg = new MicroPipelineComponentConfiguration();
		componentCfg.setId("test-component");
		componentCfg.setName("test");
		componentCfg.setSettings(null);
		componentCfg.setToQueue("test-destination-queue");
		componentCfg.setType(MicroPipelineComponentType.SOURCE);
		componentCfg.setVersion("test-version");
		
		sampleConfiguration.getComponents().add(componentCfg);
		sampleConfiguration.getQueues().add(new StreamingMessageQueueConfiguration("test-destination-queue"));
		
		MicroPipelineInstantiationResponse pipelineInstantiationResponse = new MicroPipelineInstantiationResponse(sampleConfiguration.getId(), MicroPipelineValidationResult.OK, "");
		String url = new StringBuffer("http://").append(processingNodeHost).append(":").append(processingNodeServicePort).append("/pipelines").toString();
		
		//////////////////////////////////////////////////////////
		// prepare mocked entities
		Client clientMock = Mockito.mock(Client.class);		
		WebTarget webTargetMock = Mockito.mock(WebTarget.class);
		Builder b1Mock = Mockito.mock(Builder.class);
		Builder b2Mock = Mockito.mock(Builder.class);		
		
		Mockito.when(clientMock.target(url)).thenReturn(webTargetMock);
		Mockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(b1Mock);
		Mockito.when(b1Mock.accept(MediaType.APPLICATION_JSON)).thenReturn(b2Mock); // required to support chained methods for retrieving client response
		Mockito.when(b2Mock.post(Mockito.isA(Entity.class), Mockito.eq(MicroPipelineInstantiationResponse.class))).thenReturn(pipelineInstantiationResponse);
		//
		//////////////////////////////////////////////////////////				
		
		SPQRNodeClient client = new SPQRNodeClient("http", processingNodeHost, processingNodeServicePort, processingNodeAdminPort, clientMock);
		MicroPipelineInstantiationResponse response = client.instantiatePipeline(sampleConfiguration);
		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("State must be 'OK'", MicroPipelineValidationResult.OK, response.getState());

		Mockito.verify(clientMock).target(url);
		Mockito.verify(webTargetMock).request(MediaType.APPLICATION_JSON);
		Mockito.verify(b1Mock).accept(MediaType.APPLICATION_JSON);
		Mockito.verify(b2Mock).post(Mockito.isA(Entity.class), Mockito.eq(MicroPipelineInstantiationResponse.class));
	}

	/**
	 * Test case for {@link SPQRNodeClient#updatePipeline(MicroPipelineConfiguration)}
	 * being provided valid input
	 */
	@Test
	public void testUpdatePipeline_withValidInput() throws Exception {

		String processingNodeHost = "processing-node-localhost";
		int processingNodeServicePort = 7070;
		int processingNodeAdminPort = 7071;
		MicroPipelineConfiguration sampleConfiguration = new MicroPipelineConfiguration();
		sampleConfiguration.setId("Test");
		
		MicroPipelineComponentConfiguration componentCfg = new MicroPipelineComponentConfiguration();
		componentCfg.setId("test-component");
		componentCfg.setName("test");
		componentCfg.setSettings(null);
		componentCfg.setToQueue("test-destination-queue");
		componentCfg.setType(MicroPipelineComponentType.SOURCE);
		componentCfg.setVersion("test-version");
		
		sampleConfiguration.getComponents().add(componentCfg);
		sampleConfiguration.getQueues().add(new StreamingMessageQueueConfiguration("test-destination-queue"));
		
		MicroPipelineInstantiationResponse pipelineInstantiationResponse = new MicroPipelineInstantiationResponse(sampleConfiguration.getId(), MicroPipelineValidationResult.OK, "");
		String url = new StringBuffer("http://").append(processingNodeHost).append(":").append(processingNodeServicePort).append("/pipelines/").append(sampleConfiguration.getId()).toString();
		
		//////////////////////////////////////////////////////////
		// prepare mocked entities
		Client clientMock = Mockito.mock(Client.class);		
		WebTarget webTargetMock = Mockito.mock(WebTarget.class);
		Builder b1Mock = Mockito.mock(Builder.class);
		Builder b2Mock = Mockito.mock(Builder.class);		
		
		Mockito.when(clientMock.target(url)).thenReturn(webTargetMock);
		Mockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(b1Mock);
		Mockito.when(b1Mock.accept(MediaType.APPLICATION_JSON)).thenReturn(b2Mock); // required to support chained methods for retrieving client response
		Mockito.when(b2Mock.put(Mockito.isA(Entity.class), Mockito.eq(MicroPipelineInstantiationResponse.class))).thenReturn(pipelineInstantiationResponse);
		//
		//////////////////////////////////////////////////////////				
		
		SPQRNodeClient client = new SPQRNodeClient("http", processingNodeHost, processingNodeServicePort, processingNodeAdminPort, clientMock);
		MicroPipelineInstantiationResponse response = client.updatePipeline(sampleConfiguration);
		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("State must be 'OK'", MicroPipelineValidationResult.OK, response.getState());
		
		Mockito.verify(clientMock).target(url);
		Mockito.verify(webTargetMock).request(MediaType.APPLICATION_JSON);
		Mockito.verify(b1Mock).accept(MediaType.APPLICATION_JSON);
		Mockito.verify(b2Mock).put(Mockito.isA(Entity.class), Mockito.eq(MicroPipelineInstantiationResponse.class));
	}
	
	/**
	 * Test case for {@link SPQRNodeClient#shutdown(String)} being provided an empty string
	 */
	@Test
	public void testShutdownPipeline_withNullInput() throws Exception {
		Client clientMock = Mockito.mock(Client.class);		
		String processingNodeHost = "processing-node-localhost";
		int processingNodeServicePort = 7070;
		int processingNodeAdminPort = 7071;
		SPQRNodeClient client = new SPQRNodeClient("http", processingNodeHost, processingNodeServicePort, processingNodeAdminPort, clientMock);
		
		MicroPipelineShutdownResponse response = client.shutdown(null);
		Assert.assertEquals("Invalid input", MicroPipelineShutdownState.PIPELINE_ID_MISSING, response.getState());	
	}
	
	/**
	 * Test case for {@link SPQRNodeClient#shutdown(String)} being provided a valid string
	 */
	@Test
	public void testShutdownPipeline_withValidInput() throws Exception {
		
		String processingNodeHost = "processing-node-localhost";
		int processingNodeServicePort = 7070;
		int processingNodeAdminPort = 7071;
		String pipelineId = "test-pipeline-id";
		
		MicroPipelineShutdownResponse pipelineShutdownResponse = new MicroPipelineShutdownResponse(pipelineId, MicroPipelineShutdownState.OK, "");
		String url = new StringBuffer("http://").append(processingNodeHost).append(":").append(processingNodeServicePort).append("/pipelines/").append(pipelineId).toString();
		
		//////////////////////////////////////////////////////////
		// prepare mocked entities
		Client clientMock = Mockito.mock(Client.class);		
		WebTarget webTargetMock = Mockito.mock(WebTarget.class);
		Builder b1Mock = Mockito.mock(Builder.class);
		Builder b2Mock = Mockito.mock(Builder.class);		
		
		Mockito.when(clientMock.target(url)).thenReturn(webTargetMock);
		Mockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(b1Mock);
		Mockito.when(b1Mock.accept(MediaType.APPLICATION_JSON)).thenReturn(b2Mock); // required to support chained methods for retrieving client response
		Mockito.when(b2Mock.delete(Mockito.eq(MicroPipelineShutdownResponse.class))).thenReturn(pipelineShutdownResponse);
		//
		//////////////////////////////////////////////////////////				
		
		SPQRNodeClient client = new SPQRNodeClient("http", processingNodeHost, processingNodeServicePort, processingNodeAdminPort, clientMock);
		MicroPipelineShutdownResponse response = client.shutdown("test-pipeline-id");
		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("State must be 'OK'", MicroPipelineShutdownState.OK, response.getState());
		
		Mockito.verify(clientMock).target(url);
		Mockito.verify(webTargetMock).request(MediaType.APPLICATION_JSON);
		Mockito.verify(b1Mock).accept(MediaType.APPLICATION_JSON);
		Mockito.verify(b2Mock).delete(Mockito.eq(MicroPipelineShutdownResponse.class));
	}
	
	
}
