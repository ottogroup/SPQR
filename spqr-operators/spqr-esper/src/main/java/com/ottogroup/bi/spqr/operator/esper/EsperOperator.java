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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.fasterxml.jackson.databind.JsonNode;
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

	/** body content will be copied as raw byte array into esper data structure */ 
	public static final String MESSAGE_BODY_TYPE_RAW = "raw";
	/** body content will be converted to JSON and copied into esper data structure */ 
	public static final String MESSAGE_BODY_TYPE_JSON = "json";	
	
	public static final String CFG_ESPER_STATEMENT = "esper.statement";
	public static final String CFG_MESSAGE_BODY_TYPE = "esper.message.bodyType";
	
	private enum MessageBodyType {
		RAW, JSON;
	}
	
	private String id = null;
	private long totalNumOfMessages = 0;
	private long numOfMessagesSinceLastResult = 0;
	private DelayedResponseOperatorWaitStrategy waitStrategy = null;
	
	private String esperStatementString = null;
	private EPServiceProvider esperServiceProvider = null;
	private EPStatement esperStatement = null;
	private MessageBodyType messageBodyType = MessageBodyType.RAW;
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		/////////////////////////////////////////////////////////////////////////////////
		// fetch an validate properties
		this.esperStatementString = properties.getProperty(CFG_ESPER_STATEMENT);
		if(StringUtils.isBlank(this.esperStatementString))
			throw new RequiredInputMissingException("Missing required ESPER statement");

		String bodyTypeStr = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_MESSAGE_BODY_TYPE)));
		if(StringUtils.equals(MESSAGE_BODY_TYPE_RAW, bodyTypeStr))
			this.messageBodyType = MessageBodyType.RAW;
		else if(StringUtils.equals(MESSAGE_BODY_TYPE_JSON, bodyTypeStr))
			this.messageBodyType = MessageBodyType.JSON;
		else
			throw new ComponentInitializationFailedException("Unexpected message body type found: " + bodyTypeStr);
				
		/////////////////////////////////////////////////////////////////////////////////
		
		Configuration esperConfiguration = new Configuration();
		Map<String, Object> jsonTypeDefinition = new HashMap<>();
		jsonTypeDefinition.put("timestamp", Long.class);
		jsonTypeDefinition.put("body", JsonNode.class);
		esperConfiguration.addEventType("json", jsonTypeDefinition);
		
		
		Map<String, Object> rawTypeDefinition = new HashMap<>();
		rawTypeDefinition.put("timestamp", Long.class);
		rawTypeDefinition.put("body", Object.class);
		esperConfiguration.addEventType("raw", rawTypeDefinition);

		this.esperServiceProvider = EPServiceProviderManager.getDefaultProvider(esperConfiguration);
		this.esperServiceProvider.initialize();

		this.esperStatement = this.esperServiceProvider.getEPAdministrator().createEPL(this.esperStatementString);
		this.esperStatement.setSubscriber(this);				
		
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
	
		switch(this.messageBodyType) {
			case JSON: {
				mapper.readTree(message.getBody());
				break;
			}
			case RAW: {
				break;
			}
		}
		
		if(this.messageBodyType == MessageBodyType.JSON) {
			mapper.readTree(message.getBody());
		} else
		
		this.provider.getEPRuntime().sendEvent(event, "json");
		
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getResult()
	 */
	public StreamingDataMessage[] getResult() {
		return null;
	}

	public void update(Map<String, Object> eventMap) {
		
		// TODO
        for(String s : eventMap.keySet()) {
        	System.out.println(s + " --> " + eventMap.get(s));
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
