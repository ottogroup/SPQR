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
package com.ottogroup.bi.spqr.pipeline.stats;

import java.io.IOException;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ChronicleQueueBuilder.IndexedChronicleQueueBuilder;
import net.openhft.chronicle.ChronicleQueueBuilder.VanillaChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.tools.ChronicleTools;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.DataValueClasses;

import org.junit.Test;

/**
 * @author mnxfst
 * @since May 6, 2015
 */
public class StatsTest {

	public static interface StatsEvent extends Byteable {
		boolean compareAndSwapOwner(int expected, int value);
	    int getOwner();
	    void setOwner(int meta);
	    void setId(long id);
	    long getId();

	    void setType(long id);
	    long getType();

	    void setTimestamp(long timestamp);
	    long getTimestamp();		
	}
	
	@Test
	public void testDirect() throws IOException {
		
		final int items = 13000000;
		final String path = System.getProperty("java.io.tmpdir") + "/direct-instance";
		
		ChronicleTools.deleteOnExit(path);
		VanillaChronicleQueueBuilder vanillaBuilder = ChronicleQueueBuilder.vanilla(path);
		vanillaBuilder.cleanupOnClose();
		
		IndexedChronicleQueueBuilder indexedBuilder = ChronicleQueueBuilder.indexed(path);
		// Items written: 1000000, Write: 625ms, Items read: 1000000, Read: 20ms
		// Items written: 10000000, Write: 976ms, Items read: 10000000, Read: 80ms
		
		// Items written: 1000000, Write: 746ms, Items read: 1000000, Read: 35ms
		// Items written: 10000000, Write: 1596ms, Items read: 10000000, Read: 154ms
		Chronicle chronicle = indexedBuilder.build();
//		Chronicle chronicle = vanillaBuilder.build();
		
		long sc = 0;
		long ec = 0;
		try  {
		    ExcerptAppender appender = chronicle.createAppender();
		    sc = System.currentTimeMillis();
	    	StatsEvent event = DataValueClasses.newDirectReference(StatsEvent.class);
		    for(int i=0; i<items; i++) {
		    	appender.startExcerpt(event.maxSize());

		        event.bytes(appender, 0);
		        event.setOwner(0);
		        event.setType(i / 10);
		        event.setTimestamp(System.currentTimeMillis());
		        event.setId(i);

		        appender.position(event.maxSize());
		        appender.finish();
		    }
		    ec = System.currentTimeMillis();
		    appender.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long sr = 0;
		long er = 0;
		int read = 0;
		try (ExcerptTailer tailer = chronicle.createTailer()) {
			StatsEvent event = DataValueClasses.newDirectReference(StatsEvent.class);
			sr = System.currentTimeMillis();
		     while(tailer.nextIndex()) {
		    	 
		         event.bytes(tailer, 0);
		         // Do something with event
		         read++;
		         tailer.finish();
		     }
		    er = System.currentTimeMillis();
		     
		     
		 } catch(Exception e) {
		     e.printStackTrace();
		 }
		
		System.out.println("Items written: " + items + ", Write: " + (ec-sc)+ "ms, Items read: " + read +", Read: " + (er-sr)+"ms");
	}
	
}
