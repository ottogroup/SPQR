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
package com.ottogroup.bi.spqr.operator.esper;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseCollector;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Test case for {@link EsperOperator}
 * @author mnxfst
 * @since May 5, 2015
 */
public class EsperOperatorTest {

	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided null as input
	 */
	@Test
	public void testInitialize_withNullInput() throws ComponentInitializationFailedException {
		try {
			new EsperOperator().initialize(null);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			//
		}
	}

	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set missing a field type
	 */
	@Test
	public void testInitialize_withPropertiesMissingFieldType() throws ComponentInitializationFailedException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from testEvent");
		
		try {
			new EsperOperator().initialize(properties);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			//
		}
	}

	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set missing a field name
	 */
	@Test
	public void testInitialize_withPropertiesMissingFieldName() throws ComponentInitializationFailedException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.String");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from testEvent");
		
		try {
			new EsperOperator().initialize(properties);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			//
		}
	}
	
	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set with invalid type
	 */
	@Test
	public void testInitialize_withPropertiesInvalidType() throws RequiredInputMissingException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.NoSuchClass");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from testEvent");
		
		try {
			new EsperOperator().initialize(properties);
			Assert.fail("Invalid input");
		} catch(ComponentInitializationFailedException e) {
			//
		}
	}

	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set showing a statement only
	 */
	@Test
	public void testInitialize_withStatementOnly() throws ComponentInitializationFailedException, RequiredInputMissingException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from spqrIn");
		
		new EsperOperator().initialize(properties);
	}

	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set missing a valid statement
	 */
	@Test
	public void testInitialize_withPropertiesWithoutStatement() throws ComponentInitializationFailedException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.String");
		
		try {
			new EsperOperator().initialize(properties);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			//
		}
	}
	
	/**
	 * Test case for {@link EsperOperator#initialize(java.util.Properties)} being provided a
	 * properties set with invalid statement
	 */
	@Test
	public void testInitialize_withPropertiesInvalidStatement() throws RequiredInputMissingException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.String");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from noSuchEventType");
		
		try {
			new EsperOperator().initialize(properties);
			Assert.fail("Invalid input");
		} catch(ComponentInitializationFailedException e) {
			//
		}
	}
	
	/**
	 * Test case for {@link EsperOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)}
	 * being provided null as input
	 */
	@Test
	public void testOnMessage_withNullInput() throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.String");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select * from spqrIn");
		
		EsperOperator operator = new EsperOperator();
		operator.initialize(properties);		
		operator.onMessage(null);		
	}

	/**
	 * Test case for {@link EsperOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)}
	 * being provided valid input
	 */
	@Test
	public void testOnMessage_withValidInput() throws RequiredInputMissingException, ComponentInitializationFailedException, InterruptedException {
		
		Properties properties = new Properties();
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_EVENT_SUFFIX, "testEvent");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_NAME_SUFFIX, "fieldName");
		properties.put(EsperOperator.CFG_ESPER_TYPE_DEF_PREFIX+"1"+EsperOperator.CFG_ESPER_TYPE_DEF_TYPE_SUFFIX, "java.lang.String");
		properties.put(EsperOperator.CFG_ESPER_STATEMENT_PREFIX+"1", "select cast(1, long) as timestamp, cast(body, java.util.Map) as body from spqrIn");
		
		final EsperOperator operator = new EsperOperator();
		operator.initialize(properties);		
		 
		DelayedResponseOperatorWaitStrategy strategy = Mockito.mock(DelayedResponseOperatorWaitStrategy.class);
		operator.setWaitStrategy(strategy);
		operator.onMessage(new StreamingDataMessage("{\"key\":\"value\"}".getBytes(), System.currentTimeMillis()));

		Mockito.verify(strategy).release();
		String content = new String(operator.getResult()[0].getBody());
		Assert.assertEquals("Values must be equal", "{\"key\":\"value\"}", content);
	}	
	
}
