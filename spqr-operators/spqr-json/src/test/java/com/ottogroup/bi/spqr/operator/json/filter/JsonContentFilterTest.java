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
package com.ottogroup.bi.spqr.operator.json.filter;


import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Test case for {@link JsonContentFilter}
 * @author mnxfst
 * @since Apr 8, 2015
 */
public class JsonContentFilterTest {

	/**
	 * Test case for {@link JsonContentFilter#initialize(java.util.Properties)} being provided null
	 */
	@Test
	public void testInitialize_withNullProperties() throws Exception {
		try {
			new JsonContentFilter().initialize(null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link JsonContentFilter#initialize(java.util.Properties)} being provided empty properties
	 */
	@Test
	public void testInitialize_withEmptyProperties() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(new Properties());
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#initialize(java.util.Properties)} being provided properties missing the pattern
	 */
	@Test
	public void testInitialize_withEmptyPattern() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		Properties props = new Properties();
		props.setProperty("field.1.path", "field.content");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#initialize(java.util.Properties)} being provided properties starting with id of 2
	 */
	@Test
	public void testInitialize_withStartID2() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		Properties props = new Properties();
		props.setProperty("field.2.path", "field");
		props.setProperty("field.2.expression", "value");
		props.setProperty("field.2.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#initialize(java.util.Properties)} being provided properties holding a valid matcher
	 */
	@Test
	public void testInitialize_withValidMatcher() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		Properties props = new Properties();
		props.setProperty("field.1.path", "field");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#onMessage(StreamingDataMessage)} being provided a pattern that matches with test content
	 */
	@Test
	public void testOnMessage_withValidContentAndMatcher() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		Properties props = new Properties();
		props.setProperty("field.1.path", "field");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);
		
		testMessage = new StreamingDataMessage("{\"field\":\"vadde\"}".getBytes(), System.currentTimeMillis());
		messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 1", 1, messages.length);
		Assert.assertEquals("Result must contain copy of input", testMessage, messages[0]);

		// some negative test as well .. although it does not fit with the test case name ;-)		
		testMessage = new StreamingDataMessage("{\"field\":\"vddde\"}".getBytes(), System.currentTimeMillis());
		messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 0", 0, messages.length);		
	}
	
	/**
	 * Test case for {@link JsonContentFilter#onMessage(StreamingDataMessage)} being provided a path that cannot be found in content
	 */
	@Test
	public void testOnMessage_withPathNotFoundInContent() throws Exception {
		StreamingDataMessage testMessage = new StreamingDataMessage("{\"field\":\"value\"}".getBytes(), System.currentTimeMillis());
		Properties props = new Properties();
		props.setProperty("field.1.path", "field.value");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(testMessage);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 0", 0, messages.length);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#onMessage(StreamingDataMessage)} being provided null as input
	 */
	@Test
	public void testOnMessage_withNullInput() throws Exception {
		Properties props = new Properties();
		props.setProperty("field.1.path", "field.value");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(null);
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 0", 0, messages.length);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#onMessage(StreamingDataMessage)} being provided an empty message as input
	 */
	@Test
	public void testOnMessage_withEmptyMessageBody() throws Exception {
		Properties props = new Properties();
		props.setProperty("field.1.path", "field.value");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(new StreamingDataMessage("".getBytes(), System.currentTimeMillis()));
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 0", 0, messages.length);
	}
	
	/**
	 * Test case for {@link JsonContentFilter#onMessage(StreamingDataMessage)} being provided a message having null set as body
	 */
	@Test
	public void testOnMessage_withNullMessageBody() throws Exception {
		Properties props = new Properties();
		props.setProperty("field.1.path", "field.value");
		props.setProperty("field.1.expression", "va..e");
		props.setProperty("field.1.type", "STRING");
		JsonContentFilter filter = new JsonContentFilter();
		filter.initialize(props);
		StreamingDataMessage[] messages = filter.onMessage(new StreamingDataMessage(null, System.currentTimeMillis()));
		Assert.assertNotNull("Result must not be null", messages);
		Assert.assertEquals("Result size must be 0", 0, messages.length);
	}
	
}
