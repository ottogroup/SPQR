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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Integrates the {@link http://espertech.com/ ESPER} project into SPQR pipelines.
 * @author mnxfst
 * @since Apr 23, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR, name="esperOperator", version="0.0.1", description="ESPER integration operator")
public class EsperOperator implements DelayedResponseOperator {
	
	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(EsperOperator.class);
	
	public static final String SPQR_EVENT_TIMESTAMP_FIELD = "timestamp";
	public static final String SPQR_EVENT_BODY_FIELD = "body";
	public static final String DEFAULT_INPUT_EVENT = "spqrIn";
	public static final String DEFAULT_OUTPUT_EVENT = "spqrOut";
	public static final String CFG_ESPER_STATEMENT_PREFIX = "esper.statement.";
	public static final String CFG_ESPER_TYPE_DEF_PREFIX = "esper.typeDef.";
	public static final String CFG_ESPER_TYPE_DEF_EVENT_SUFFIX = ".event";
	public static final String CFG_ESPER_TYPE_DEF_NAME_SUFFIX = ".name";
	public static final String CFG_ESPER_TYPE_DEF_TYPE_SUFFIX = ".type";
	
	private String id = null;
	private long totalNumOfMessages = 0;
	private long numOfMessagesSinceLastResult = 0;
	private DelayedResponseOperatorWaitStrategy waitStrategy = null;
	
	private EPServiceProvider esperServiceProvider = null;
	private EPRuntime esperRuntime = null;
	private final ObjectMapper mapper = new ObjectMapper();

	private StreamingDataMessage[] result = null;
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		if(properties == null)
			throw new RequiredInputMissingException("Missing required properties");
		
		/////////////////////////////////////////////////////////////////////////////////
		// fetch an validate properties
		Set<String> esperQueryStrings = new HashSet<>();
		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			String tmpStr = properties.getProperty(CFG_ESPER_STATEMENT_PREFIX+i);
			if(StringUtils.isBlank(tmpStr))
				break;
			esperQueryStrings.add(StringUtils.trim(tmpStr));
		}
		if(esperQueryStrings.isEmpty())
			throw new RequiredInputMissingException("Missing required ESPER statement(s)");
		/////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////
		// fetch event configuration
		Map<String, Map<String, String>> eventConfiguration = new HashMap<>();
		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			final String typeDefEvent = StringUtils.trim(properties.getProperty(CFG_ESPER_TYPE_DEF_PREFIX + i + CFG_ESPER_TYPE_DEF_EVENT_SUFFIX));
			if(StringUtils.isBlank(typeDefEvent))
				break;
			final String typeDefName = StringUtils.trim(properties.getProperty(CFG_ESPER_TYPE_DEF_PREFIX + i + CFG_ESPER_TYPE_DEF_NAME_SUFFIX));
			final String typeDefType = StringUtils.trim(properties.getProperty(CFG_ESPER_TYPE_DEF_PREFIX + i + CFG_ESPER_TYPE_DEF_TYPE_SUFFIX));
			
			if(StringUtils.isBlank(typeDefName) || StringUtils.isBlank(typeDefType))
				throw new RequiredInputMissingException("Missing type def name or type for event '"+typeDefEvent+"' at position " + i);
			
			Map<String, String> ec = eventConfiguration.get(typeDefEvent);
			if(ec == null)
				ec = new HashMap<>();
			ec.put(typeDefName, typeDefType);
			eventConfiguration.put(typeDefEvent, ec);
			
		}
		///////////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////////
		// create esper configuration

		Configuration esperConfiguration = new Configuration();
		for(final String event : eventConfiguration.keySet()) {
			Map<String, String> ec = eventConfiguration.get(event);
			if(ec != null && !ec.isEmpty()) {
				Map<String, Object> typeDefinition = new HashMap<>();
				for(final String typeDefName : ec.keySet()) {
					final String typeDefType = ec.get(typeDefName);					
					try {
						typeDefinition.put(typeDefName, Class.forName(typeDefType));
					} catch(ClassNotFoundException e) {
						throw new ComponentInitializationFailedException("Failed to lookup provided type '"+typeDefType+"' for event '"+event+"'. Error: " + e.getMessage());
					}
				}
				esperConfiguration.addEventType(event, typeDefinition);			
			}			
		}

		Map<String, Object> spqrDefaultTypeDefinition = new HashMap<>();
		spqrDefaultTypeDefinition.put(SPQR_EVENT_TIMESTAMP_FIELD, Long.class);
		spqrDefaultTypeDefinition.put(SPQR_EVENT_BODY_FIELD, Map.class);
		esperConfiguration.addEventType(DEFAULT_INPUT_EVENT, spqrDefaultTypeDefinition);
		esperConfiguration.addEventType(DEFAULT_OUTPUT_EVENT, spqrDefaultTypeDefinition);
		///////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////
		// initialize service provider, submit statements and retrieve runtime 
		this.esperServiceProvider = EPServiceProviderManager.getDefaultProvider(esperConfiguration);
		this.esperServiceProvider.initialize();

		for(final String qs : esperQueryStrings) {
			try {
				EPStatement esperStatement = this.esperServiceProvider.getEPAdministrator().createEPL(qs);
				esperStatement.setSubscriber(this);
			} catch(EPStatementException e) {
				throw new ComponentInitializationFailedException("Failed to parse query into ESPER statement. Error: " + e.getMessage(), e);
			}
		}
		
		this.esperRuntime = this.esperServiceProvider.getEPRuntime();
		///////////////////////////////////////////////////////////////////////////////////
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
		
		if(message == null || message.getBody() == null)
			return;
		
		Map<String, Object> event = new HashMap<String, Object>();
		event.put("timestamp", message.getTimestamp());
		try {
			event.put("body", mapper.readValue(message.getBody(), new TypeReference<Map<String, Object>>() {}));
		} catch(IOException e) {
			logger.error("Failed to parse incoming message to structured JSON map. Error: " + e.getMessage());
			event.put("body", Collections.emptyMap());
		}
		
		this.esperRuntime.sendEvent(event, DEFAULT_INPUT_EVENT);
		this.numOfMessagesSinceLastResult++;
		this.totalNumOfMessages++;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getResult()
	 */
	public StreamingDataMessage[] getResult() {
		this.numOfMessagesSinceLastResult = 0;
		return result;
	}

	/**
	 * Callback invoked by ESPER for nay result  
	 * @param eventMap
	 */
	public void update(Map<String, Object> eventMap) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>)eventMap.get(SPQR_EVENT_BODY_FIELD);
		Long timestamp = (Long)eventMap.get(SPQR_EVENT_TIMESTAMP_FIELD);

		if(body != null) {
			try {
				byte[] messageBody = mapper.writeValueAsBytes(body);
				if(messageBody != null && messageBody.length > 0) {
					result = new StreamingDataMessage[]{new StreamingDataMessage(messageBody, (timestamp != null ? timestamp.longValue() : System.currentTimeMillis()))};
					this.waitStrategy.release();
				}
			} catch(IOException e) {
				logger.error("Failed to parse ESPER result to JSON representation. Error: " + e.getMessage(), e);
			}
		}
    }
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getNumberOfMessagesSinceLastResult()
	 */
	public long getNumberOfMessagesSinceLastResult() {
		return this.numOfMessagesSinceLastResult;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.Operator#getTotalNumOfMessages()
	 */
	public long getTotalNumOfMessages() {
		return this.totalNumOfMessages;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#setWaitStrategy(com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy)
	 */
	public void setWaitStrategy(DelayedResponseOperatorWaitStrategy waitStrategy) {
		this.waitStrategy = waitStrategy;
	}

}
