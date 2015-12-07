package cn.creditease.test.framework;

import java.awt.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class ReflectorHelper {
	public static Method getMethod(Class<?> classObj, String methodName) {
		Method[] allMethods = classObj.getDeclaredMethods();
		Method selectedMethod = null;
		for (Method method : allMethods) {
			if (method.getName().equalsIgnoreCase(methodName)) {
				selectedMethod = method;
				break;
			}
		}
		
		return selectedMethod;
	}
	
	public static Object populateInstance(Class<?> classObj, Map<String, String> map) {
		Field[] fieldList = classObj.getDeclaredFields();
		for (Field field : fieldList) {

		}
		
		return null;
	}
	
	public static Map<String, Object> objectToMap(Object obj) {
		String jsonStr = JSON.toJSONString(obj);
		return JSON.parseObject(jsonStr, Map.class);
	} 
	
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		Class<?> classObj = Class.forName(className);
		return classObj;
	}

	public static <T> T createInstance(Class<T> paramterType, Object value) {
		String jsonStr = JSON.toJSONString(value);
		return JSON.parseObject(jsonStr, paramterType);
	}
}
