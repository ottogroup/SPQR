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
package com.ottogroup.bi.spqr.operator.json.aggregator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Aggregates content of JSON documents provided
 * @author mnxfst
 * @since Mar 17, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR, name="jsonContentAggregator", version="0.0.1", description="Aggregates arbitrary JSON content")
public class JsonContentAggregator implements DelayedResponseOperator {

	/** our faithful logging facility .... ;-) */
	private static final Logger logger = Logger.getLogger(JsonContentAggregator.class);
	
	////////////////////////////////////////////////////////////////////////
	// available settings
	/** if provided the pipeline identifier is added to output document */
	public static final String CFG_PIPELINE_ID = "pipelineId";
	/** type assigned to each output document */
	public static final String CFG_DOCUMENT_TYPE = "documentType";
	/** store and forward raw data - default: true */
	public static final String CFG_FORWARD_RAW_DATA = "forwardRawData";
	/** prefix to all field settings - required: field.1.name, field.1.path and field.1.type (settings must use continuous enumeration starting with value 1 */
	public static final String CFG_FIELD_PREFIX = "field.";
	//
	////////////////////////////////////////////////////////////////////////

	/**
	 * Allowed values types: STRING or NUMERICAL
	 * @author mnxfst
	 * @since Nov 30, 2014
	 */
	enum ValueType implements Serializable {
		STRING, NUMERICAL, UNKNOWN 
	}

	/** component identifier assigned by caller */
	private String id = null;
	/** maps inbound strings into object representations and json strings vice versa */
	private final ObjectMapper jsonMapper = new ObjectMapper();
	/** identifier as assigned to surrounding pipeline */
	private String pipelineId = null;
	/** document identifier added to each output message */
	private String documentType = null;
	/** overall number of messages processed */ 
	private long messageCount = 0;
	/** number of messages processed since last result collection */	
	private long messagesSinceLastResult = 0;
	/** store and forward raw data - default: true */
	private boolean storeForwardRawData = true;
	/** fields considered to be relevant mapped to aggregator that must be applied to values - none = data is added to raw output only */
	private List<JsonContentAggregatorFieldSetting> fields = new ArrayList<>();
	/** result document - reset after specified duration */
	private JsonContentAggregatorResult resultDocument = null;

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException,	ComponentInitializationFailedException {
		
		/////////////////////////////////////////////////////////////////////////////////////
		// assign and validate properties
		if(StringUtils.isBlank(this.id))
			throw new RequiredInputMissingException("Missing required component identifier");
		
		this.pipelineId = StringUtils.trim(properties.getProperty(CFG_PIPELINE_ID));		
		this.documentType = StringUtils.trim(properties.getProperty(CFG_DOCUMENT_TYPE));
		if(StringUtils.equalsIgnoreCase(properties.getProperty(CFG_FORWARD_RAW_DATA), "false"))
			this.storeForwardRawData = false;
		

		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			String name = properties.getProperty(CFG_FIELD_PREFIX + i + ".name");
			if(StringUtils.isBlank(name))
				break;
			
			String path = properties.getProperty(CFG_FIELD_PREFIX + i + ".path");
			String valueType = properties.getProperty(CFG_FIELD_PREFIX + i + ".type");
			
			this.fields.add(new JsonContentAggregatorFieldSetting(name, path.split("\\."), StringUtils.equalsIgnoreCase("STRING", valueType) ? ValueType.STRING : ValueType.NUMERICAL));
		}
		/////////////////////////////////////////////////////////////////////////////////////
		
		if(logger.isDebugEnabled())
			logger.debug("json content aggregator [id="+id+"] initialized");
		
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {		
		this.messageCount++;
		this.messagesSinceLastResult++;
		
		
		// do nothing if either the event or the body is empty
		if(message == null || message.getBody() == null || message.getBody().length < 1)
			return;
		
		JsonNode jsonNode = null;
		try {
			jsonNode = jsonMapper.readTree(message.getBody());
		} catch(IOException e) {
			logger.error("Failed to read message body to json node. Ignoring message. Error: " + e.getMessage());
		}
		
		// return null in case the message could not be parsed into 
		// an object representation - the underlying processor does
		// not forward any NULL messages
		if(jsonNode == null)
			return;
		
		// initialize the result document if not already done
		if(this.resultDocument == null)
			this.resultDocument = new JsonContentAggregatorResult(this.pipelineId, this.documentType);
		
		Map<String, Object> rawData = new HashMap<>();
		// step through fields considered to be relevant, extract values and apply aggregation function
		for(final JsonContentAggregatorFieldSetting fieldSettings : fields) {
			
			// switch between string and numerical field values
			// string values may be counted only
			// numerical field values must be summed, min and max computed AND counted 
			
			// string values may be counted only
			if(fieldSettings.getValueType() == ValueType.STRING) {

				try {
					// read value into string representation and add it to raw data dump
					String value = getTextFieldValue(jsonNode, fieldSettings.getPath());
					if(storeForwardRawData)
						rawData.put(fieldSettings.getField(), value);
					
					// count occurrences of value
					try {
						this.resultDocument.incAggregatedValue(fieldSettings.getField(), value, 1);
					} catch (RequiredInputMissingException e) {
						logger.error("Field '"+fieldSettings.getField()+"' not found in event. Ignoring value. Error: " +e.getMessage());
					}
				} catch(Exception e) {
				}
			} else if(fieldSettings.getValueType() == ValueType.NUMERICAL) {			
				
				try {
					// read value into numerical representation and add it to raw data map
					long value = getNumericalFieldValue(jsonNode, fieldSettings.getPath());
					if(storeForwardRawData)
						rawData.put(fieldSettings.getField(), value);
					
					// compute min, max and sum and add these values to result document
					try {
						this.resultDocument.evalMinAggregatedValue(fieldSettings.getField(), "min", value);
						this.resultDocument.evalMaxAggregatedValue(fieldSettings.getField(), "max", value);
						this.resultDocument.incAggregatedValue(fieldSettings.getField(), "sum", value);
					} catch(RequiredInputMissingException e) {
						logger.error("Field '"+fieldSettings.getField()+"' not found in event. Ignoring value. Error: " +e.getMessage());
					}				
					
				} catch(Exception e) {
				}
			}			
		}
		
		// add raw data to document
		if(storeForwardRawData)
			this.resultDocument.addRawData(rawData);
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getResult()
	 */
	public StreamingDataMessage[] getResult() {
		this.messagesSinceLastResult = 0;
		
		StreamingDataMessage message = null;
		try {			
			message = new StreamingDataMessage(jsonMapper.writeValueAsBytes(this.resultDocument), System.currentTimeMillis());
		} catch (JsonProcessingException e) {
			logger.error("Failed to convert result document into JSON");
		}
		this.resultDocument = new JsonContentAggregatorResult(this.pipelineId, this.documentType);
		return new StreamingDataMessage[]{message};
	}

	/**
	 * Walks along the path provided and reads out the leaf value which is returned as string 
	 * @param jsonNode
	 * @param fieldPath
	 * @return
	 */
	protected String getTextFieldValue(final JsonNode jsonNode, final String[] fieldPath) {

		int fieldAccessStep = 0;
		JsonNode contentNode = jsonNode;
		while(fieldAccessStep < fieldPath.length) {
			contentNode = contentNode.get(fieldPath[fieldAccessStep]);
			fieldAccessStep++;
		}	

		return contentNode.textValue();
	}
	
	/**
	 * Walks along the path provided and reads out the leaf value which is returned as long value
	 * @param jsonNode
	 * @param fieldPath
	 * @return
	 */
	protected long getNumericalFieldValue(final JsonNode jsonNode, final String[] fieldPath) {

		int fieldAccessStep = 0;
		JsonNode contentNode = jsonNode;
		while(fieldAccessStep < fieldPath.length) {
			contentNode = contentNode.get(fieldPath[fieldAccessStep]);
			fieldAccessStep++;
		}	

		return contentNode.asLong();
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getNumberOfMessagesSinceLastResult()
	 */
	public long getNumberOfMessagesSinceLastResult() {
		return this.messagesSinceLastResult;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.Operator#getTotalNumOfMessages()
	 */
	public long getTotalNumOfMessages() {
		return this.messageCount;
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

}
