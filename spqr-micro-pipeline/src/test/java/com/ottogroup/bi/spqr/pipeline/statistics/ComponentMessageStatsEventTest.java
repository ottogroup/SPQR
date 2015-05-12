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


import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.ottogroup.bi.spqr.pipeline.statistics.ComponentMessageStatsEvent;

/**
 * Test case for {@link ComponentMessageStatsEvent}
 * @author mnxfst
 * @since May 12, 2015
 */
public class ComponentMessageStatsEventTest {

	/**
	 * Test case for {@link ComponentMessageStatsEvent#fromBytes(byte[])} being provided null
	 * as input
	 */
	@Test
	public void testFromBytes_withNullInput() {
		ComponentMessageStatsEvent event = ComponentMessageStatsEvent.fromBytes(null);
		Assert.assertNotNull("The event must not be null", event);
		Assert.assertTrue("The component id must be empty", StringUtils.isBlank(event.getComponentId()));
		Assert.assertEquals("Values must be equal", 0, event.getTimestamp());
		Assert.assertEquals("Values must be equal", 0, event.getDuration());
		Assert.assertEquals("Values must be equal", 0, event.getSize());
		Assert.assertFalse("Must be false", event.isError());
	}
	
	/**
	 * Test case for {@link ComponentMessageStatsEvent#fromBytes(byte[])} being provided a 
	 * valid event 
	 */
	@Test
	public void testFromBytes_withValidEvent() {
		
		ComponentMessageStatsEvent inEvent = new ComponentMessageStatsEvent("test-id", true, 123, 456, 789);
		byte[] inEventContent = inEvent.toBytes();
		
		ComponentMessageStatsEvent event = ComponentMessageStatsEvent.fromBytes(inEventContent);
		Assert.assertNotNull("The event must not be null", event);
		Assert.assertEquals("Values must be equal", "test-id", event.getComponentId());
		Assert.assertEquals("Values must be equal", 789, event.getTimestamp());
		Assert.assertEquals("Values must be equal", 123, event.getDuration());
		Assert.assertEquals("Values must be equal", 456, event.getSize());
		Assert.assertTrue("Must be true", event.isError());
	}
	
}
