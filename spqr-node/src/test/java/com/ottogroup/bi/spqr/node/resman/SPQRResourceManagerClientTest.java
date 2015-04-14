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
package com.ottogroup.bi.spqr.node.resman;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationState;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Test case for {@link SPQRResourceManagerClient}
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class SPQRResourceManagerClientTest {

	/**
	 * Test case for {@link SPQRResourceManagerClient#SPQRResourceManagerClient(String, String, int, com.sun.jersey.api.client.Client)} being
	 * provided an empty input to protocol parameter
	 */
	@Test	
	public void testConstructor_withEmptyProtocol() {
		try {
			new SPQRResourceManagerClient("", "localhost", 9090, Mockito.mock(Client.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRResourceManagerClient#SPQRResourceManagerClient(String, String, int, com.sun.jersey.api.client.Client)} being
	 * provided an empty input to host parameter
	 */
	@Test	
	public void testConstructor_withEmptyRemoteHost() {
		try {
			new SPQRResourceManagerClient("http", "", 9090, Mockito.mock(Client.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRResourceManagerClient#SPQRResourceManagerClient(String, String, int, com.sun.jersey.api.client.Client)} being
	 * provided an invalid input to port parameter
	 */
	@Test	
	public void testConstructor_withInvalidPort() {
		try {
			new SPQRResourceManagerClient("http", "localhost", -1, Mockito.mock(Client.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRResourceManagerClient#SPQRResourceManagerClient(String, String, int, com.sun.jersey.api.client.Client)} being
	 * provided valid input
	 */
	@Test	
	public void testConstructor_withValidInput() throws RequiredInputMissingException {
		SPQRResourceManagerClient client = new SPQRResourceManagerClient("http", "localhost", 8080, Mockito.mock(Client.class));
		Assert.assertNotNull("The client must not be null", client);
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#registerNode(String, String, int, int)} being provided an 
	 * empty input to protocol parameter
	 */
	@Test
	public void testRegisterNode_withEmptyNodeProtocol() throws Exception {
		try {
			new SPQRResourceManagerClient("http", "resourceManager", 9090).registerNode("", "localhost", 8080, 8081);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#registerNode(String, String, int, int)} being provided an 
	 * empty input to host parameter
	 */
	@Test
	public void testRegisterNode_withEmptyNodeHost() throws Exception {
		try {
			new SPQRResourceManagerClient("http", "localhost", 9090).registerNode("http", "", 8080, 8081);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#registerNode(String, String, int, int)} being provided an 
	 * invalid input to service port parameter
	 */
	@Test
	public void testRegisterNode_withInvalidNodeServicePort() throws Exception {
		try {
			new SPQRResourceManagerClient("http", "resourceManager", 9090).registerNode("http", "localhost", -1, 8081);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#registerNode(String, String, int, int)} being provided an 
	 * invalid input to admin port parameter
	 */
	@Test
	public void testRegisterNode_withInvalidNodeAdminPort() throws Exception {
		try {
			new SPQRResourceManagerClient("http", "resourceManager", 9090).registerNode("http", "localhost", 8080, -1);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#registerNode(String, String, int, int)} being provided
	 * valid input that must lead to successful registration
	 */
	@Test
	public void testRegisterNode_withValidInput() throws Exception {
		
		String remoteUrl = "http://localhost:7070/nodes";
		ObjectMapper mapper = new ObjectMapper();
		String requestString = mapper.writeValueAsString(new NodeRegistrationRequest("http", "localhost", 8080, 8081)); 
		String responseString = mapper.writeValueAsString(new NodeRegistrationResponse("test-id", NodeRegistrationState.OK));
		
		ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
		Mockito.when(mockClientResponse.getEntity(String.class)).thenReturn(responseString);
		
		Builder mockBuilderType = Mockito.mock(Builder.class);
		Mockito.when(mockBuilderType.post(ClientResponse.class, requestString)).thenReturn(mockClientResponse);
		
		Builder mockBuilderAccept = Mockito.mock(Builder.class);
		Mockito.when(mockBuilderAccept.type(MediaType.APPLICATION_JSON)).thenReturn(mockBuilderType);		
		
		WebResource mockWebResource = Mockito.mock(WebResource.class);
		Mockito.when(mockWebResource.accept(MediaType.APPLICATION_JSON)).thenReturn(mockBuilderAccept);
		
		Client mockClient = Mockito.mock(Client.class);
		Mockito.when(mockClient.resource(remoteUrl)).thenReturn(mockWebResource);

		SPQRResourceManagerClient client = new SPQRResourceManagerClient("http", "localhost", 7070, mockClient);
		NodeRegistrationResponse response = client.registerNode("http", "localhost", 8080, 8081);
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("No errors expected", NodeRegistrationState.OK, response.getState());
		
		Mockito.verify(mockClient).resource(remoteUrl);
		Mockito.verify(mockWebResource).accept(MediaType.APPLICATION_JSON);
		Mockito.verify(mockBuilderAccept).type(MediaType.APPLICATION_JSON);
		Mockito.verify(mockClientResponse).getEntity(String.class);
	}
	
	/**
	 * Test case for {@link SPQRResourceManagerClient#deregisterNode(String)} being provided empty input
	 */
	@Test
	public void testDeRegisterNode_withEmptyInput() throws Exception {
		try {
			new SPQRResourceManagerClient("http", "localhost", 8080).deregisterNode(null);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	
	/**
	 * Test case for {@link SPQRResourceManagerClient#deregisterNode(String)} being provided valid input
	 */
	@Test
	public void testDeRegisterNode_withValidInput() throws Exception {
		
		String remoteUrl = "http://localhost:7070/nodes/test-id";
		ObjectMapper mapper = new ObjectMapper();
		String responseString = mapper.writeValueAsString(new NodeDeRegistrationResponse("test-id", NodeDeRegistrationState.OK, ""));
		
		ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
		Mockito.when(mockClientResponse.getEntity(String.class)).thenReturn(responseString);
		
		Builder mockBuilderType = Mockito.mock(Builder.class);
		Mockito.when(mockBuilderType.delete(ClientResponse.class)).thenReturn(mockClientResponse);
		
		Builder mockBuilderAccept = Mockito.mock(Builder.class);
		Mockito.when(mockBuilderAccept.type(MediaType.APPLICATION_JSON)).thenReturn(mockBuilderType);		
		
		WebResource mockWebResource = Mockito.mock(WebResource.class);
		Mockito.when(mockWebResource.accept(MediaType.APPLICATION_JSON)).thenReturn(mockBuilderAccept);
		
		Client mockClient = Mockito.mock(Client.class);
		Mockito.when(mockClient.resource(remoteUrl)).thenReturn(mockWebResource);

		SPQRResourceManagerClient client = new SPQRResourceManagerClient("http", "localhost", 7070, mockClient);
		NodeDeRegistrationResponse response = client.deregisterNode("test-id");
		Assert.assertNotNull("The response must not be null", response);
		Assert.assertEquals("No errors expected", NodeDeRegistrationState.OK, response.getState());
		
		Mockito.verify(mockClient).resource(remoteUrl);
		Mockito.verify(mockWebResource).accept(MediaType.APPLICATION_JSON);
		Mockito.verify(mockBuilderAccept).type(MediaType.APPLICATION_JSON);
		Mockito.verify(mockClientResponse).getEntity(String.class);
	}

}
