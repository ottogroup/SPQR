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

import io.dropwizard.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationState;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;

/**
 * Test case for {@link SPQRNodeManager}
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class SPQRNodeManagerTest {

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, int, int)} being provided an empty protocol
	 */
	@Test
	public void testRegisterNode_withEmptyProtocol() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.MISSING_PROTOCOL, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).registerNode("", "localhost", 8080, 9090).getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, int, int)} being provided an empty host
	 */
	@Test
	public void testRegisterNode_withEmptyRemoteHost() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.MISSING_HOST, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).registerNode("http", "", 8080, 9090).getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, int, int)} being provided an invalid service port
	 */
	@Test
	public void testRegisterNode_withInvalidServicePort() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.MISSING_SERVICE_PORT, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).registerNode("http", "localhost", -1, 9090).getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, int, int)} being provided an invalid admin port
	 */
	@Test
	public void testRegisterNode_withInvalidAdminPort() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.MISSING_ADMIN_PORT, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).registerNode("http", "localhost", 8080, -1).getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, int, int)} where node id computation fails
	 */
	@Test
	public void testRegisterNode_withFailedNodeIdComputation() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.NODE_ID_COMPUTATION_FAILED, new SPQRNodeManager(0, Mockito.mock(JerseyClientBuilder.class)).registerNode("http", "localhost", 8080, 9090).getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#registerNode(String, String, int, int)} being provided valid input
	 */
	@Test
	public void testRegisterNode_withValidInput() {
		
		Client mockClient = Mockito.mock(Client.class);
		
		JerseyClientBuilder mockClientBuilder = Mockito.mock(JerseyClientBuilder.class);
		Mockito.when(mockClientBuilder.build(Mockito.anyString())).thenReturn(mockClient);
		
		SPQRNodeManager manager = new SPQRNodeManager(5, mockClientBuilder);
		NodeRegistrationResponse response = manager.registerNode("http", "localhost", 8080, 9090);
		Assert.assertEquals("Node must be registered by now", NodeRegistrationState.OK, response.getState());
		Assert.assertTrue("Node must be registered by now", manager.hasNode(response.getId()));
		
		Mockito.verify(mockClientBuilder).build(Mockito.anyString());
	}

	/**
	 * Test case for {@link SPQRNodeManager#deregisterNode(String)} being provided an empty string
	 */
	@Test
	public void testDeRegisterNode_withEmptyInput() {
		Assert.assertEquals("Invalid input", NodeDeRegistrationState.MISSING_NODE_ID, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).deregisterNode("").getState());
	}

	/**
	 * Test case for {@link SPQRNodeManager#deregisterNode(String)} being provided an unknown node id
	 */
	@Test
	public void testDeRegisterNode_withUnknownNodeId() {
		Assert.assertEquals("Invalid input", NodeDeRegistrationState.NO_SUCH_NODE_ID, new SPQRNodeManager(5, Mockito.mock(JerseyClientBuilder.class)).deregisterNode("unknown-id").getState());
	}
	
	/**
	 * Test case for {@link SPQRNodeManager#deregisterNode(String)} being provided the id of an already
	 * registered node
	 */
	@Test
	public void testDeRegisterNode_withExistingNodeId() {
		
		Client mockClient = Mockito.mock(Client.class);
		
		JerseyClientBuilder mockClientBuilder = Mockito.mock(JerseyClientBuilder.class);
		Mockito.when(mockClientBuilder.build(Mockito.anyString())).thenReturn(mockClient);
		
		SPQRNodeManager manager = new SPQRNodeManager(5, mockClientBuilder);
		NodeRegistrationResponse response = manager.registerNode("http", "localhost", 8080, 9090);
		Assert.assertEquals("Node must be registered by now", NodeRegistrationState.OK, response.getState());
		Assert.assertTrue("Node must be registered by now", manager.hasNode(response.getId()));
		
		NodeDeRegistrationResponse deRegistrationResponse = manager.deregisterNode(response.getId());
		Assert.assertEquals("De-Registration should be succesful", NodeDeRegistrationState.OK, deRegistrationResponse.getState());
		Assert.assertEquals("Ids must be equal", response.getId(), deRegistrationResponse.getId());
		
		deRegistrationResponse = manager.deregisterNode(response.getId());
		Assert.assertEquals("Node already de-registered", NodeDeRegistrationState.NO_SUCH_NODE_ID, deRegistrationResponse.getState());
		Assert.assertFalse("Node is not registered anymore", manager.hasNode(response.getId()));
		
		Mockito.verify(mockClientBuilder).build(Mockito.anyString());
	}

	
}
