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
package com.ottogroup.bi.spqr.node.server;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RemoteClientConnectionFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;
import com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient;

/**
 * Test case for {@link SPQRNodeServer}
 * @author mnxfst
 * @since Apr 15, 2015
 */
public class SPQRNodeServerTest {

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided an empty string to protocol property
	 */
	@Test
	public void testRegisterProcessingNode_withEmptyProtocol() throws Exception {
		try {
			new SPQRNodeServer().registerProcessingNode("", "localhost", 8080, 8081, Mockito.mock(SPQRResourceManagerClient.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided an empty string to host property
	 */
	@Test
	public void testRegisterProcessingNode_withEmptyHost() throws Exception {
		try {
			new SPQRNodeServer().registerProcessingNode("http", "", 8080, 8081, Mockito.mock(SPQRResourceManagerClient.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided an invalid input to service port property
	 */
	@Test
	public void testRegisterProcessingNode_withInvalidServicePort() throws Exception {
		try {
			new SPQRNodeServer().registerProcessingNode("http", "localhost", -1, 8081, Mockito.mock(SPQRResourceManagerClient.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided an invalid input to admin port property
	 */
	@Test
	public void testRegisterProcessingNode_withInvalidAdminPort() throws Exception {
		try {
			new SPQRNodeServer().registerProcessingNode("http", "localhost", 8080, -1, Mockito.mock(SPQRResourceManagerClient.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided valid input but null returned by client
	 */
	@Test
	public void testRegisterProcessingNode_withNullReturnedByClient() throws Exception {
		SPQRResourceManagerClient mockClient = Mockito.mock(SPQRResourceManagerClient.class);
		Mockito.when(mockClient.registerNode("http", "localhost", 8080, 8081)).thenReturn(null);
		try {
			new SPQRNodeServer().registerProcessingNode("http", "localhost", 8080, 8081, mockClient);
			Assert.fail("Client returned null");
		} catch(RemoteClientConnectionFailedException e) {
			//
		}
		
		Mockito.verify(mockClient).registerNode("http", "localhost", 8080, 8081);
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided valid input but the client returns an error
	 */
	@Test
	public void testRegisterProcessingNode_withErrorReturnedByClient() throws Exception {
		SPQRResourceManagerClient mockClient = Mockito.mock(SPQRResourceManagerClient.class);
		Mockito.when(mockClient.registerNode("http", "localhost", 8080, 8081)).thenReturn(new NodeRegistrationResponse("test", NodeRegistrationState.NODE_ID_COMPUTATION_FAILED));
		try {
			new SPQRNodeServer().registerProcessingNode("http", "localhost", 8080, 8081, mockClient);
			Assert.fail("Client returned null");
		} catch(RemoteClientConnectionFailedException e) {
			//
		}
		
		Mockito.verify(mockClient).registerNode("http", "localhost", 8080, 8081);
	}

	/**
	 * Test case for {@link SPQRNodeServer#registerProcessingNode(String, String, int, int, com.ottogroup.bi.spqr.node.resman.SPQRResourceManagerClient)} being
	 * provided valid input and client returned valid id
	 */
	@Test
	public void testRegisterProcessingNode_withValidIdReturnedByClient() throws Exception {
		SPQRResourceManagerClient mockClient = Mockito.mock(SPQRResourceManagerClient.class);
		Mockito.when(mockClient.registerNode("http", "localhost", 8080, 8081)).thenReturn(new NodeRegistrationResponse("test-id", NodeRegistrationState.OK));
		Assert.assertEquals("The identifier must be as expected", "test-id", new SPQRNodeServer().registerProcessingNode("http", "localhost", 8080, 8081, mockClient));
		Mockito.verify(mockClient).registerNode("http", "localhost", 8080, 8081);
	}
	
}
