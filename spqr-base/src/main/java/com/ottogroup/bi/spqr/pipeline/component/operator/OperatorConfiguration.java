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
package com.ottogroup.bi.spqr.pipeline.component.operator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Describes the operator
 * @author mnxfst
 * @since Mar 5, 2015
 */
@JsonRootName(value="operatorConfiguration")
public class OperatorConfiguration implements Serializable {

	private static final long serialVersionUID = -2761386244558266748L;

	/** operator identifier which must be unique within the boundaries of the {@link MicroPipeline} the operator belongs to */
	@JsonProperty(value="id", required=true)
	private String id = null;	
	/** inbox identifier which must be unique within the boundaries of the {@link MicroPipeline} the operator belongs to */
	@JsonProperty(value="inbox", required=true)
	private String inboxId = null;
	/** optional inbox settings - typically provided by {@link MicroPipeline} in case a different implementation is selected in favor of the default */
	@JsonProperty(value="inboxSettings", required=false)
	private Properties inboxSettings = new Properties();
	/** outboxes the operator distributes its output to - only the identifiers are provided as additional configuration from the inbox configuration */
	@JsonProperty(value="outboxes", required=true)
	private Map<String, String> outboxIds = new HashMap<>();
	
	
}
