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
package com.ottogroup.bi.spqr.operator.esper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

/**
 * @author mnxfst
 * @since Apr 23, 2015
 */
public class EsperOperatorDepr {

	private EPServiceProvider provider = null;
	private EPStatement statement = null;
	
	public void test() throws Exception  {
		Configuration cfg = new Configuration();
		cfg.addEventType("hash", HashMap.class);

		Map<String, Object> nestedDef = new HashMap<String, Object>();
		nestedDef.put("text", String.class);
		
		Map<String, Object> def = new HashMap<String, Object>();
		def.put("id", int.class);
		def.put("timestamp", Date.class);
		def.put("content", nestedDef);
		cfg.addEventType("json", def);
		
		this.provider = EPServiceProviderManager.getDefaultProvider(cfg);
		this.provider.initialize();
//		TestSubscriber subscriber = new TestSubscriber();
		
		this.statement = this.provider.getEPAdministrator().createEPL(this.getStatement());
		this.statement.setSubscriber(this);
		

		Map<String, Object> content = new HashMap<String, Object>();
		content.put("text", "test-content");
		
		for(int i = 0; i < 100; i++) {
			Map<String, Object> event = new HashMap<String, Object>();
			event.put("id", i);
			event.put("timestamp", new Date());
			event.put("content", content);
			
			
			this.provider.getEPRuntime().sendEvent(event, "json");
		}
		
		Thread.sleep(5100);
	}
	
	public static void main(String[] args) throws Exception {
		new EsperOperatorDepr().test();
	}
	
	public void update(Map<String, Object> eventMap) {
        for(String s : eventMap.keySet()) {
        	System.out.println(s + " --> " + eventMap.get(s));
        }
    }
	
	public String getStatement() {
//        return "select content.text from json";
		return "select * from json.win:time_batch(5 sec)";
		
	}
	
}
