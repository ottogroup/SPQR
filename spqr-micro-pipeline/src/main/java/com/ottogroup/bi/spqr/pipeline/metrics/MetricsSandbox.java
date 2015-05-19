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
package com.ottogroup.bi.spqr.pipeline.metrics;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

/**
 * @author mnxfst
 *
 */
public class MetricsSandbox {

	static final MetricRegistry metrics = new MetricRegistry();
	
	public static void main(String[] args) throws Exception {
		startReport();
//		Meter req = metrics.meter("requests");
		Histogram gram = metrics.histogram("histo");
//		Counter count = metrics.counter("counter");
//		Timer timer = metrics.timer("timer");
//		Timer.Context ctx = timer.time();
//		Thread.sleep(1234);
//		ctx.stop();
		
		
//		count.inc();
//		count.inc(18);
//		
		gram.update(10);
		gram.update(10);
		gram.update(20);
		gram.update(20);
		gram.update(20);
		gram.update(20);
		gram.update(20);
		gram.update(25);
		gram.update(25);
		gram.update(15);
//		req.mark();
//		req.mark();
		
		wait5Seconds();
//		for(int i = 0; i < 100; i++)
//			req.mark();
//		wait5Seconds();
		System.out.println(MetricRegistry.name(MetricsSandbox.class, "runme"));

		for(String k : metrics.getHistograms().keySet()) {
			Histogram g = metrics.getHistograms().get(k);
			System.out.println(k + ": " + g.getSnapshot().get999thPercentile());
		}
		
		System.out.println(MetricRegistry.name("first", "second", "third", "fourth", "fifth"));
		
	}
	
	static void startReport() {
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
