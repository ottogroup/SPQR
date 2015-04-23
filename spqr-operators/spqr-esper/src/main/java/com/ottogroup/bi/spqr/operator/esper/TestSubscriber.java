package com.ottogroup.bi.spqr.operator.esper;

import java.util.Map;

public class TestSubscriber {
	
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
