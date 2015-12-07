package cn.creditease.test.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.util.StringUtils;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.db.DatabaseFactory;
import cn.creditease.test.framework.db.SQLDb;
import cn.creditease.test.framework.serviceClient.ClientEngine;
import cn.creditease.test.framework.serviceClient.ClientEngineFactory;
import cn.creditease.test.framework.serviceConfig.ServiceConfigItem;
import cn.creditease.test.framework.serviceConfig.ServiceConfigLoader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class CETestUtil {
	private static final Logger logger = LoggerFactory.getLogger(CETestUtil.class);

	public static final String NotEmpty = "notempty";
	public static final String NULLValue = "null";

	private static ClientEngineFactory clientEngine = ClientEngineFactory.getInstance();
	private static DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

	public static Map<String, Object> call(String serviceId, Map<String, Object> request) {
		try {
			ClientEngine engine = clientEngine.get(serviceId);
			if (engine == null) {
				String errMessage = String.format("Not found service config for [%s]", serviceId);

				logger.error(errMessage);
				throw new RuntimeException(errMessage);
			}

			Map<String, Object> response = engine.execute(request);
			return response;
		} catch (Exception ex) {
			if (ex.getMessage() == null) {
				logger.error("Encount error: fail to service invoke, please check if the service is avaliable or have correct config");
				throw new RuntimeException("fail to service invoke, please check if the service is avaliable or have correct config");
			} else {
				logger.error("Encount error: " + ex.getMessage());
				throw new RuntimeException(ex.getMessage());
			}
		}
	}

	public static void executeSql(String database, String sql) {
		SQLDb engine = databaseFactory.getEngine(database);
		engine.execute(sql);
	}

	public static List<Map<String, Object>> query(String database, String sql) {
		SQLDb engine = databaseFactory.getEngine(database);
		return engine.queryForList(sql);
	}

	public static void compareResult(List<Map<String, Object>> actual, List<Map<String, Object>> expected, String msgPrefix) {
		if ((actual == null || expected == null)) {
			String errMessage = String.format("[%s compare failed]: actual or expected result has null value", msgPrefix);

			logger.error(errMessage);
			throw new RuntimeException(errMessage);
		}

		if (actual.size() != expected.size()) {
			String errMessage = String.format("[%s compare failed]: result not match \n ActualResult: %s \n ExpectedResult: %s",
							msgPrefix, JSON.toJSONString(actual),
							JSON.toJSONString(expected));

			logger.error(errMessage);
			throw new RuntimeException(errMessage);
		}

		for (int index = 0; index < actual.size(); index++) {
			compareResult(actual.get(index), expected.get(index), msgPrefix);
		}
	}

	public static void compareResult(List<Map<String, Object>> actual, Map<String, Object> expected, String msgPrefix) {
		if ((actual == null || expected == null)) {
			String errMessage = String.format("[%s compare failed]: actual or expected result has null value", msgPrefix);

			logger.error(errMessage);
			throw new RuntimeException(errMessage);
		}

		if (actual.size() != 1) {
			String errMessage = String.format("[%s compare failed]: result not match \n ActualResult: %s \n ExpectedResult: %s",
							msgPrefix, JSON.toJSONString(actual),
							JSON.toJSONString(expected));

			logger.error(errMessage);
			throw new RuntimeException(errMessage);
		}

		compareResult(actual.get(0), expected, msgPrefix);
	}

	public static void compareResult(Map actualResult, Map expectedResult, String msgPrefix) {
		if ((actualResult == null || expectedResult == null)) {
			String errMessage = String.format("[%s compare failed]: actual or expected result has null value", msgPrefix);
			logger.info(errMessage);
			throw new RuntimeException(errMessage);
		}

		StringBuilder filedMismatch = new StringBuilder();
		int index = 1;
		Map actual = new TreeMap<String, Object>(actualResult);
		Map expected = new TreeMap<String, Object>(expectedResult);
		for (Object key : expected.keySet()) {
			if (!actual.containsKey(key)) {
				filedMismatch.append(String.format("%s. [%s] field not found in actual result:\n", index++, key));
				continue;
			}

			String expectedValue = String.valueOf(expected.get(key));
			String actualValue = String.valueOf(actual.get(key));
			if (expectedValue.equalsIgnoreCase(NotEmpty)) {
				if (actualValue.equalsIgnoreCase(NULLValue) || StringUtils.isEmpty(actualValue.trim())) {
					filedMismatch.append(String.format("%s. [%s] field expected=NotEmpty\n", index++, key));
				}

				continue;
			}

			if (expectedValue.equalsIgnoreCase(NULLValue)) {
				if (!actualValue.equalsIgnoreCase(NULLValue)) {
					filedMismatch.append(String.format("%s. [%s] field expected=null, actual=%s \n",
							 index++, key, actualValue));
				}

				continue;
			}

			if (!expectedValue.equalsIgnoreCase(actualValue)) {
				filedMismatch.append(String.format("%s. [%s] field expected=%s, actual=%s \n",
							index++, key, expectedValue, actualValue));
			}
		}

		if (filedMismatch.length() > 0) {
			String errMessag = String.format("[%s compare failed]: Mismatch found \n%s ActualResult: %s \n ExpectedResult: %s",
							msgPrefix, filedMismatch.toString(), 
							JSON.toJSONString(actual),
							JSON.toJSONString(expected));
			logger.info(errMessag);
			throw new RuntimeException(errMessag);
		}
	}


}
