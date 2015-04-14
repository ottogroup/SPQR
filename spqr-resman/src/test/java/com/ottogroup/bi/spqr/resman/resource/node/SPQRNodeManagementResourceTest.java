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
package com.ottogroup.bi.spqr.resman.resource.node;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeDeRegistration.NodeDeRegistrationState;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationResponse;
import com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationState;
import com.ottogroup.bi.spqr.resman.node.SPQRNodeManager;

/**
 * Test case for {@link SPQRNodeManagementResource}
 * @author mnxfst
 * @since Apr 14, 2015
 */
public class SPQRNodeManagementResourceTest {
	
	/**
	 * Test case for {@link SPQRNodeManagementResource#registerNode(com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest)} being provided
	 * null as input
	 */
	@Test
	public void testRegisterNode_withNullInput() {
		Assert.assertEquals("Invalid input", NodeRegistrationState.MISSING_REQUEST, new SPQRNodeManagementResource(Mockito.mock(SPQRNodeManager.class)).registerNode(null).getState());
	}
	
	/**
	 * Test case for {@link SPQRNodeManagementResource#registerNode(com.ottogroup.bi.spqr.node.message.NodeRegistration.NodeRegistrationRequest)} being provided
	 * valid input 
	 */
	@Test
	public void testRegisterNode_withValidInput() {
		SPQRNodeManager mockedNodeManager = Mockito.mock(SPQRNodeManager.class);
		NodeRegistrationRequest request = new NodeRegistrationRequest("http", "localhost", 8080, 8081);
		Mockito.when(mockedNodeManager.registerNode(request.getProtocol(), request.getHost(), request.getServicePort(), request.getAdminPort())).
			thenReturn(new NodeRegistrationResponse("test",  NodeRegistrationState.OK));
		Assert.assertEquals("Valid input provided", NodeRegistrationState.OK, new SPQRNodeManagementResource(mockedNodeManager).registerNode(request).getState());
		Mockito.verify(mockedNodeManager).registerNode(request.getProtocol(), request.getHost(), request.getServicePort(), request.getAdminPort());		
	}
	
	/**
	 * Test case for {@link SPQRNodeManagementResource#deregisterNode(String)} being provided null
	 */
	@Test
	public void testDeRegisterNode_withNullInput() {
		SPQRNodeManager manager = Mockito.mock(SPQRNodeManager.class);
		Mockito.when(manager.deregisterNode(null)).thenReturn(new NodeDeRegistrationResponse("", NodeDeRegistrationState.MISSING_NODE_ID, ""));
		Assert.assertEquals("Invalid input", NodeDeRegistrationState.MISSING_NODE_ID, new SPQRNodeManagementResource(manager).deregisterNode(null).getState());
	}
	
}
