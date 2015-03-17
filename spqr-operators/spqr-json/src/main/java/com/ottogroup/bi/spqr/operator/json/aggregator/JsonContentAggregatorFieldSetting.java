/**
 * Copyright 2014 Otto (GmbH & Co KG)
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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.ottogroup.bi.spqr.operator.json.aggregator.JsonContentAggregator.ValueType;

/**
 * Stores all relevant information required for aggregating json content field data
 * @author mnxfst
 * @since Nov 12, 2014
 */
@JsonRootName ( value = "eciLiveFieldSettings" )
public class JsonContentAggregatorFieldSetting implements Serializable {

	private static final long serialVersionUID = 6129451872974489460L;

	@JsonProperty ( value = "field", required = true )
	private String field = null;
	@JsonProperty ( value = "path", required = true )
	private String[] path = null;
	@JsonProperty ( value = "valueType", required = true )
	private ValueType valueType = ValueType.UNKNOWN;
	
	/**
	 * Default constructor
	 */
	public JsonContentAggregatorFieldSetting() {		
	}
	
	/**
	 * Initializes the settings using the provided input
	 * @param field
	 * @param path
	 * @param valueType
	 */
	public JsonContentAggregatorFieldSetting(final String field, final String[] path, final ValueType valueType) {
		this.field = field;
		this.path = path;
		this.valueType = valueType;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
}
