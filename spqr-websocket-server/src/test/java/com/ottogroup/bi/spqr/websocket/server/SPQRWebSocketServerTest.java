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
package com.ottogroup.bi.spqr.websocket.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Test case for {@link SPQRWebSocketServer}
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRWebSocketServerTest {

	/**
	 * Test case for {@link SPQRWebSocketServer#run(String)} being provided an empty
	 * file reference
	 */
	@Test
	public void testRun_withEmptyFileReference() throws Exception {
		try {
			new SPQRWebSocketServer().run("");
			Assert.fail("Invalid input");
		} catch(IOException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRWebSocketServer#run(String)} being provided an invalid
	 * file reference
	 */
	@Test
	public void testRun_withInvalidFileReference() throws Exception {
		try {
			new SPQRWebSocketServer().run("/no/such/folder/no-such-file-"+System.currentTimeMillis()+"-.cfg");
			Assert.fail("Invalid input");
		} catch(IOException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRWebSocketServer#run(java.io.InputStream)} being provided a
	 * stream holding a non-JSON string
	 */
	@Test
	public void testRun_withStreamHoldingNonJSONString() throws Exception {
		try {
			new SPQRWebSocketServer().run(new ByteArrayInputStream("no-json".getBytes()));
			Assert.fail("Invalid input");
		} catch(JsonParseException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link SPQRWebSocketServer#run(java.io.InputStream)} being provided a
	 * stream holding a JSON string which does not comply with expected format
	 */
	@Test
	public void testRun_withStreamHoldingInvalidJSONFormat() throws Exception {
		try {
			new SPQRWebSocketServer().run(new ByteArrayInputStream("{\"field\":\"value\"}".getBytes()));
			Assert.fail("Invalid input");
		} catch(UnrecognizedPropertyException e) {
			// expected
		}
	}
	
}
