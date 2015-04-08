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

import com.ottogroup.bi.spqr.operator.json.JsonContentType;

/**
 * Stores all relevant information required for aggregating json content field data
 * @author mnxfst
 * @since Nov 12, 2014
 */
public class JsonContentAggregatorFieldSetting implements Serializable {

	private static final long serialVersionUID = 6129451872974489460L;

	private String field = null;
	private String[] path = null;
	private JsonContentType valueType = JsonContentType.UNKNOWN;
	
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
	public JsonContentAggregatorFieldSetting(final String field, final String[] path, final JsonContentType valueType) {
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

	public JsonContentType getValueType() {
		return valueType;
	}

	public void setValueType(JsonContentType valueType) {
		this.valueType = valueType;
	}
	
}
