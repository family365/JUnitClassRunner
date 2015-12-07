package cn.creditease.test.framework;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;

public class GlobalProperty {
	private static final Logger logger = LoggerFactory.getLogger(GlobalProperty.class);
	private static final String DefaultPropertyFile = "CEUnit.properties";
	
	static Map<String, String> properties = new HashMap<String, String>();
	static GlobalProperty instance = null;

	private GlobalProperty() {
	}
	
	public static GlobalProperty getInstance() {
		if (instance == null) {
			synchronized (GlobalProperty.class) {
				if (instance == null) {
					instance = new GlobalProperty();
				}
			}
		}

		return instance;
	}
	
	public Set<String> getPropertyKeySet() {
		return properties.keySet();
	}

	public String getValue(String key) {
		if (!properties.containsKey(key)) {
			throw new RuntimeException("Cannot found the value for " + key);
		}

		return properties.get(key);
	}
	
	public void init(String propertyFile) {
		logger.info("Start to load the global properties");
		Properties prop = new Properties();
		try {
			if (propertyFile == null || propertyFile.trim().length() < 1) {
				propertyFile = DefaultPropertyFile;
			}
			
			InputStream stream = getClass().getClassLoader().getResourceAsStream(propertyFile);
			if (stream == null) {
				logger.error(String.format("Property file %s not found", propertyFile));
				throw new FileNotFoundException(String.format("Property file %s not found", propertyFile));
			}
			
			prop.load(stream);
			Set<Object> keyList = prop.keySet();
			for (Object obj : keyList) {
				String key = (String) obj;
				String value = prop.getProperty(key);
				properties.put(key, value);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			throw new RuntimeException(ex.getMessage());
		}
		
		logger.info("Finish to load the global properties");
	}
}
