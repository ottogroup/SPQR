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
package com.ottogroup.bi.spqr.pipeline.stats.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.lang.model.Byteable;

/**
 * {@link Chronicle} based queue for transporting stats information
 * @author mnxfst
 * @since May 6, 2015
 */
public class StatisticsQueue <E extends Byteable>{
	
	private Chronicle chronicleQueue = null;

	
	
}
