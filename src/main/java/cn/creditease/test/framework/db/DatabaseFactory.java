package cn.creditease.test.framework.db;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.GlobalProperty;

enum ConnectionItemEnum {
	DatabaseType, Driver, URL, UserName, Password
};

public class DatabaseFactory {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);
	
	private static final Pattern patternDatabaseType = Pattern.compile("(\\w+)\\.database", Pattern.CASE_INSENSITIVE);
	private static final Pattern patternDriver = Pattern.compile("(\\w+)\\.driverClassName", Pattern.CASE_INSENSITIVE);
	private static final Pattern patternUrl = Pattern.compile("(\\w+)\\.url", Pattern.CASE_INSENSITIVE);
	private static final Pattern patternUserName = Pattern.compile("(\\w+)\\.username", Pattern.CASE_INSENSITIVE);
	private static final Pattern patternPassword = Pattern.compile("(\\w+)\\.password", Pattern.CASE_INSENSITIVE);

	private static Map<ConnectionItemEnum, Pattern> configPatterns = new HashMap<ConnectionItemEnum, Pattern>();
	private static Map<String, SQLDb> sqlEngines = new HashMap<String, SQLDb>();
	private static DatabaseFactory instance = null;

	static {
		configPatterns.put(ConnectionItemEnum.DatabaseType,patternDatabaseType);
		configPatterns.put(ConnectionItemEnum.Driver, patternDriver);
		configPatterns.put(ConnectionItemEnum.URL, patternUrl);
		configPatterns.put(ConnectionItemEnum.UserName, patternUserName);
		configPatterns.put(ConnectionItemEnum.Password, patternPassword);
	}
	
	private DatabaseFactory() {
	}
	
	public static DatabaseFactory getInstance() {
		if (instance == null) {
			synchronized (DatabaseFactory.class) {
				if (instance == null) {
					instance = new DatabaseFactory();
				}
			}
		}
		
		return instance;
	}

	public void init() {
		Map<String, SQLConnectionDTO> sqlConnectionMap = getAllDatabaseConfig();
		createDBEngine(sqlConnectionMap);
	}
	
	public SQLDb getEngine(String key) {
		SQLDb engine = sqlEngines.get(key);
		if (engine == null) {
			String errMessage = String.format("NOT found config for database %s", key);
			logger.error(errMessage);
			throw new RuntimeException(errMessage);
		}
		
		return engine;
	}
	
	private Map<String, SQLConnectionDTO> getAllDatabaseConfig() {
		Map<String, SQLConnectionDTO> connConfigMap = new HashMap<String, SQLConnectionDTO>();
		GlobalProperty properites = GlobalProperty.getInstance();

		for (String key : properites.getPropertyKeySet()) {
			ConnectionItemEnum configItemType = isDatabaseConfig(key);
			if (configItemType == null) {
				continue;
			}

			Pattern pattern = configPatterns.get(configItemType);
			String databaseId = getDatabaseId(pattern, key);
			if (!connConfigMap.containsKey(databaseId)) {
				connConfigMap.put(databaseId, new SQLConnectionDTO());
			}

			SQLConnectionDTO conn = connConfigMap.get(databaseId);
			String value = properites.getValue(key);
			if (ConnectionItemEnum.DatabaseType == configItemType) {
				conn.setDatabaseType(value);
			} else if (ConnectionItemEnum.Driver == configItemType) {
				conn.setDriver(value);
			} else if (ConnectionItemEnum.URL == configItemType) {
				conn.setUrl(value);
			} else if (ConnectionItemEnum.UserName == configItemType) {
				conn.setUserName(value);
			} else if (ConnectionItemEnum.Password == configItemType) {
				conn.setPassword(value);
			}
		}
		
		logger.info(String.format("Success to load database config: %s", connConfigMap.toString()));
		return connConfigMap;
	}

	private void createDBEngine(Map<String, SQLConnectionDTO> configList) {
		for(String key : configList.keySet()) {
			SQLConnectionDTO sqlConfig = configList.get(key);
			if (sqlConfig.getDatabaseType().equalsIgnoreCase("mysql")) {
				SQLDb mysql = new MySQLDb(sqlConfig);
				sqlEngines.put(key, mysql);
			}
		}
		
		logger.info("Success to init database engine");
	}

	private ConnectionItemEnum isDatabaseConfig(String config) {
		for (ConnectionItemEnum configType : configPatterns.keySet()) {
			Pattern pattern = configPatterns.get(configType);
			Matcher matcher = pattern.matcher(config);
			if (matcher.find()) {
				return configType;
			}
		}

		return null;
	}

	private String getDatabaseId(Pattern configItemType, String config) {
		Matcher matcher = configItemType.matcher(config);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
