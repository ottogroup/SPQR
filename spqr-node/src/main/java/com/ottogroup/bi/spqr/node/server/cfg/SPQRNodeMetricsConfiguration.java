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
package com.ottogroup.bi.spqr.node.server.cfg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.ottogroup.bi.spqr.metrics.MetricsHandler;

/**
 * Settings required for configuring an optional {@link MetricsHandler}
 * @author mnxfst
 * @since May 21, 2015
 */
@JsonRootName(value="spqrNodeMetricsConfiguration")
public class SPQRNodeMetricsConfiguration implements Serializable {

	private static final long serialVersionUID = 3555151965536734286L;

	@JsonProperty(value="graphite", required=false)
	private MetricsGraphiteReporterConfiguration graphite = null;
	
}
