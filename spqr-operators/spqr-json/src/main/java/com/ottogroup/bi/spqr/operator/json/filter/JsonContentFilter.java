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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.operator.json.JsonContentType;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperator;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Filters the content of incoming {@link StreamingDataMessage} for specific content. All non-matching messages
 * are removed from the pipeline, all matching messages get passed on to the next {@link MicroPipelineComponent}.
 * <br/><br/>
 * To configure a content filter instance the properties must show the following settings: (id = enumeration value starting with value 1)
 * <ul>
 *   <li><i>field.[id].path</i> - path to field (eg. data.wt.cs-host)</li>
 *   <li><i>field.[id].expression</i> - regular expression applied on field content (see {@linkplain http://en.wikipedia.org/wiki/Regular_expression} for more information)</li>
 *   <lI><i>field.[id].type</i> - string, numerical or boolean (required for content conversion and expression application: type-to-string)</li>
 * </ul> 
 * @author mnxfst
 * @since Apr 8, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR, name="jsonContentFilter", version="0.0.1", description="Filters arbitrary JSON content")
public class JsonContentFilter implements DirectResponseOperator {

	/** our faithful logging facility .... ;-) */ 
	private static final Logger logger = Logger.getLogger(JsonContentFilter.class);	
	/** empty array of streaming data messages required when holding back messages not matching with required patterns */
	private static final StreamingDataMessage[] EMPTY_MESSAGES_ARRAY = new StreamingDataMessage[0];
	
	/** prefix to all field settings - required: field.1.path, field.1.expression and field.1.type (settings must use continuous enumeration starting with value 1) */
	public static final String CFG_FIELD_PREFIX = "field.";

	/** unique component identifier */
	private String id = null;
	/** number of messages processed since initialization */
	private int totalNumOfMessages = 0;
	/** fields considered to be relevant mapped to aggregator that must be applied to values - none = data is added to raw output only */
	private final List<JsonContentFilterFieldSetting> fields = new ArrayList<>();
	/** maps inbound strings into object representations and json strings vice versa */
	private final ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		if(properties == null)
			throw new RequiredInputMissingException("Missing required properties");
		
		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			String expression = properties.getProperty(CFG_FIELD_PREFIX + i + ".expression");
			if(StringUtils.isBlank(expression))
				break;
			
			String path = properties.getProperty(CFG_FIELD_PREFIX + i + ".path");
			String valueType = properties.getProperty(CFG_FIELD_PREFIX + i + ".type");
			
			try {
				this.fields.add(new JsonContentFilterFieldSetting(path.split("\\."), Pattern.compile(expression), StringUtils.equalsIgnoreCase("STRING", valueType) ? JsonContentType.STRING : JsonContentType.NUMERICAL));
			} catch(PatternSyntaxException e) {
				throw new ComponentInitializationFailedException("Failed to parse '"+expression+"' into a valid pattern expression");
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("json content filter [id="+id+"] initialized");		
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public StreamingDataMessage[] onMessage(StreamingDataMessage message) {
		
		// increment number of messages processed so far 
		this.totalNumOfMessages++; 
		
		// do nothing if either the event or the body is empty
		if(message == null || message.getBody() == null || message.getBody().length < 1)
			return EMPTY_MESSAGES_ARRAY;
		
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
			return EMPTY_MESSAGES_ARRAY;

		// step through fields considered to be relevant, extract values and apply filtering function
		for(final JsonContentFilterFieldSetting fieldSettings : fields) {
			
			// read value into string representation for further investigation
			String value = getTextFieldValue(jsonNode, fieldSettings.getPath());
			
			if(!fieldSettings.getExpression().matcher(StringUtils.trim(value)).matches())
				return EMPTY_MESSAGES_ARRAY;
		}
		
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

		if(contentNode != null)
			return contentNode.textValue();
		return "";
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR;
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

}
