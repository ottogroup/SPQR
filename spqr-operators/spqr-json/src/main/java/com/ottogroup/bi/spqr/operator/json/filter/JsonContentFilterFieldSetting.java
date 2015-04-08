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

import java.util.regex.Pattern;

import com.ottogroup.bi.spqr.operator.json.JsonContentType;


/**
 * Holds the settings for each field configured for {@link JsonContentFilter}
 * @author mnxfst
 * @since Apr 8, 2015
 */
public class JsonContentFilterFieldSetting {
	
	private final String[] path;
	private final Pattern expression;
	private final JsonContentType valueType;

	/**
	 * Initializes the settings using the provided input
	 * @param path
	 * @param expression
	 * @param valueType
	 */
	public JsonContentFilterFieldSetting(final String[] path, final Pattern expression, final JsonContentType valueType) {
		this.expression = expression;
		this.path = path;
		this.valueType = valueType;
	}

	public String[] getPath() {
		return path;
	}

	public Pattern getExpression() {
		return expression;
	}

	public JsonContentType getValueType() {
		return valueType;
	}


}
