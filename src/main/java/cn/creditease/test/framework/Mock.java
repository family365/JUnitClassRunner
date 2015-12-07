package cn.creditease.test.framework;

import java.util.HashMap;
import java.util.Map;

import cn.creditease.test.framework.serviceClient.HttpClientEngine;

public class Mock {
	static String mockAddr = null;
	static {
		GlobalProperty prop = GlobalProperty.getInstance();
		mockAddr = prop.getValue("mock");
	}
	
	public static void mock(String url, String param) {
		url = mockAddr + url;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("target", "mock");
		String[] keyValueList = param.split(";");
		for(String pair : keyValueList) {
			String[] fields = pair.split("=");
			map.put(fields[0], fields[1]);
		}
		
		Map<String, Object> result = HttpClientEngine.doGet(url, map);
	}
}
