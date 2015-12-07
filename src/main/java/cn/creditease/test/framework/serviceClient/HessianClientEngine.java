package cn.creditease.test.framework.serviceClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.ReflectorHelper;
import cn.creditease.test.framework.exception.EngineInitException;
import cn.creditease.test.framework.serviceConfig.ServiceConfigItem;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.client.HessianProxyFactory;

public class HessianClientEngine implements ClientEngine {
	private static final Logger logger = LoggerFactory
			.getLogger(HessianClientEngine.class);

	private Object serviceInstance = null;
	private Class<?> interfaceClassObj = null;
	private String methodName = "";
	private String url = "";

	public HessianClientEngine(ServiceConfigItem config) {
		url = config.getUrl();
		methodName = config.getMethod();
		String interfaceName = config.getInterfaceName();

		try {
			interfaceClassObj = ReflectorHelper.loadClass(config
					.getInterfaceName());
			HessianProxyFactory hessianProxy = new HessianProxyFactory();
			serviceInstance = hessianProxy.create(interfaceClassObj, url);
		} catch (ClassNotFoundException e) {
			logger.error(String.format(
					"Interface not found when initializing hessian client: %s",
					interfaceName));
			throw new EngineInitException(e);
		} catch (MalformedURLException ex) {
			logger.error(String
					.format("Hessian URL path is malformed: %s", url));
			throw new EngineInitException(ex);
		}
	}

	public static void checkParameter(ServiceConfigItem config) {
		if (config.getUrl() == null || config.getUrl().trim().length() == 0) {
			throw new RuntimeException(
					"url parameter for Hessian request is not valid");
		}

		if (config.getInterfaceName() == null
				|| config.getInterfaceName().trim().length() == 0) {
			throw new RuntimeException("Hessian interface not specify");
		}

		if (config.getMethod() == null
				|| config.getMethod().trim().length() == 0) {
			throw new RuntimeException(
					"Method name for hessian request not specify");
		}
	}

	public Map<String, Object> execute(Map<String, Object> param) {
		logger.info(String.format("Hessian request %s/%s with parameter %s",
				url, methodName, param.toString()));
		Method method = ReflectorHelper
				.getMethod(interfaceClassObj, methodName);
		Class<?>[] parameterTypeList = method.getParameterTypes();
		Object returnObj = null;

		try {
			if (parameterTypeList.length == 0) {
				returnObj = method.invoke(serviceInstance);
			} else if (parameterTypeList.length > 1) {
				if (parameterTypeList.length != param.size()) {
					throw new RuntimeException("parameter is not valid");
				}

				if (!(param instanceof LinkedHashMap)) {
					throw new RuntimeException("A LinkedHashMap type of parameter is expected");
				}

				Object parameterList[] = new Object[param.size()];
				int index = 0;
				for (Entry<String, Object> each : param.entrySet()) {
					Class<?> paramType = parameterTypeList[index];
					Object obj = ReflectorHelper.createInstance(paramType,
							each.getValue());
					parameterList[index] = obj;
				}

				returnObj = method.invoke(serviceInstance, parameterList);
			} else if (parameterTypeList.length == 1) {
				Class<?> paramType = parameterTypeList[0];
				Object requestParam = ReflectorHelper.createInstance(paramType,
						param);
				returnObj = method.invoke(serviceInstance, requestParam);

			}

			if (returnObj != null) {
				logger.info("Hessian response: " + JSON.toJSONString(returnObj));
				return ReflectorHelper.objectToMap(returnObj);
			}
			
			return null;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
