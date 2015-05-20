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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mnxfst
 *
 */
public class MetricsSandbox {

	static final MetricRegistry metrics = new MetricRegistry();
	
	public static void main(String[] args) throws Exception {
//		startReport();
////		Meter req = metrics.meter("requests");
////		Histogram gram = metrics.histogram("histo");
////		Counter count = metrics.counter("counter");
//		Timer timer = metrics.timer("timer");
//		Timer.Context ctx = timer.time();
//		Thread.sleep(1234);
//		ctx.stop();
//		
//		
////		count.inc();
////		count.inc(18);
////		
////		gram.update(10);
////		gram.update(10);
////		gram.update(20);
////		gram.update(20);
////		gram.update(20);
////		gram.update(20);
////		gram.update(20);
////		gram.update(25);
////		gram.update(25);
////		gram.update(15);
////		req.mark();
////		req.mark();
//
//		
//		metrics.register(MetricRegistry.name(MetricsSandbox.class, "test", "size"),
//                new Gauge<Integer>() {
//                    @Override
//                    public Integer getValue() {
//                        return new Random().nextInt(100);
//                    }
//                });
//		
//		wait5Seconds();
////		for(int i = 0; i < 100; i++)
////			req.mark();
////		wait5Seconds();
//		System.out.println(MetricRegistry.name(MetricsSandbox.class, "runme"));
//
//		for(String k : metrics.getHistograms().keySet()) {
//			Histogram g = metrics.getHistograms().get(k);
//			System.out.println(k + ": " + g.getSnapshot().get999thPercentile());
//		}
//		
//		System.out.println(MetricRegistry.name("first", "second", "third", "fourth", "fifth"));
		
		FileInputStream fin = new FileInputStream("");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int c = 0;
		while((c = fin.read()) != -1) {
			bout.write(c);
		}
		fin.close();
		
		
		ObjectMapper mapper = new ObjectMapper();
		byte[] msg = bout.toByteArray();
		
//
//	    // Parse byte array to Map
//	    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
//	    ObjectInputStream in = new ObjectInputStream(byteIn);
//	    Map<Integer, String> data2 = (Map<Integer, String>) in.readObject();
//	    System.out.println(data2.toString());
//		
//		
		mapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
	    mapper.readTree(msg);
		
		
		for(int i = 0; i < 5; i++) {
		
		long s1 = System.currentTimeMillis();
		Map content = mapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
		long s2 = System.currentTimeMillis();		
		JsonNode jsonNode = mapper.readTree(msg);
		long s3 = System.currentTimeMillis();

		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    ObjectOutputStream out = new ObjectOutputStream(byteOut);
	    out.writeObject(content);
	    long s4 = System.currentTimeMillis();
	    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
	    ObjectInputStream in = new ObjectInputStream(byteIn);
	    Map<Integer, String> data2 = (Map<Integer, String>) in.readObject();
	    long s5 = System.currentTimeMillis();	
		
	    if(i == 4) {
		System.out.println("to map: " + (s2-s1)+"ms");
		System.out.println("to node: " + (s3-s2)+"ms");
		System.out.println("to baos: " + (s4-s3)+"ms");
		System.out.println("from baos: " + (s5-s4)+"ms");
	    }
		}
		
	}
	
	static void startReport() {
//	      CsvReporter reporter = CsvReporter.forRegistry(metrics)
//	          .convertRatesTo(TimeUnit.SECONDS)
//	          .convertDurationsTo(TimeUnit.MILLISECONDS)
//	          .build(new File("/tmp/data"));
		
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
		          .convertRatesTo(TimeUnit.SECONDS)
		          .convertDurationsTo(TimeUnit.MILLISECONDS)
		          .build();
		
	      reporter.start(1, TimeUnit.SECONDS);
	  }

	  static void wait5Seconds() {
	      try {
	          Thread.sleep(5*1000);
	      }
	      catch(InterruptedException e) {}
	  }
}
