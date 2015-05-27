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
package com.ottogroup.bi.spqr.metrics;

import com.codahale.metrics.Counter;

/**
 * Extends the default {@link Counter} implementation such that the
 * value is reset to 0 when calling {@link #getCount()} 
 * @author mnxfst
 * @since May 21, 2015
 */
public class ResetOnReadCounter extends Counter {

	/**
	 * @see com.codahale.metrics.Counter#getCount()
	 */
	public long getCount() {
		long val = super.getCount();
		super.dec(val);
		return val;
	}	
	
}


