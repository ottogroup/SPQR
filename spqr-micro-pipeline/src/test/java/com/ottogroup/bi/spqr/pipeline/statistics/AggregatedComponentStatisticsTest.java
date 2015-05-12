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
package com.ottogroup.bi.spqr.pipeline.statistics;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link AggregatedComponentStatistics}
 * @author mnxfst
 * @since May 12, 2015
 */
public class AggregatedComponentStatisticsTest {

	/**
	 * Test case for {@link AggregatedComponentStatistics#toBytes()} and {@link AggregatedComponentStatistics#fromBytes(byte[])} 
	 * invoked with valid input
	 */
	@Test
	public void testToAndFromBytes_withValidInput() {
		
		int idSize = "test".getBytes().length;
		
		int expectedArraySize = idSize + 9 * AggregatedComponentStatistics.SIZE_OF_INT + 2 * AggregatedComponentStatistics.SIZE_OF_LONG;
		
		AggregatedComponentStatistics stats = new AggregatedComponentStatistics("test", 123, 456, 654, 789, 987, 456, 908, 809, 989, 2);
		byte[] content = stats.toBytes();
		Assert.assertNotNull("Must not be null", content);
		Assert.assertEquals("Values must be equal", expectedArraySize, content.length);
		
		AggregatedComponentStatistics reStats = AggregatedComponentStatistics.fromBytes(content);
		Assert.assertNotNull("Must not be null", reStats);
		Assert.assertEquals("Values must be equal", stats.getEndTime(), reStats.getEndTime());
		Assert.assertEquals("Values must be equal", stats.getStartTime(), reStats.getStartTime());
		Assert.assertEquals("Values must be equal", stats.getAvgDuration(), reStats.getAvgDuration());
		Assert.assertEquals("Values must be equal", stats.getAvgSize(), reStats.getAvgSize());
		Assert.assertEquals("Values must be equal", stats.getComponentId(), reStats.getComponentId());
		Assert.assertEquals("Values must be equal", stats.getErrors(), reStats.getErrors());
		Assert.assertEquals("Values must be equal", stats.getMaxDuration(), reStats.getMaxDuration());
		Assert.assertEquals("Values must be equal", stats.getMaxSize(), reStats.getMaxSize());
		Assert.assertEquals("Values must be equal", stats.getMinDuration(), reStats.getMinDuration());
		Assert.assertEquals("Values must be equal", stats.getMinSize(), reStats.getMinSize());
		Assert.assertEquals("Values must be equal", stats.getNumOfMessages(), reStats.getNumOfMessages());
	}
	
}
