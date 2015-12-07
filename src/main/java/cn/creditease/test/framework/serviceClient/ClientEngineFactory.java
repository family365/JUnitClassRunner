package cn.creditease.test.framework.serviceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.serviceConfig.ServiceConfigItem;
import cn.creditease.test.framework.serviceConfig.ServiceConfigLoader;

public class ClientEngineFactory {
	private static final Logger logger = LoggerFactory.getLogger(ClientEngineFactory.class);
	
	private static ClientEngineFactory instance = null;
	private Map<String, ClientEngine> engineMap = new HashMap<String, ClientEngine>();
	
	private ClientEngineFactory() {
	}
	
	public static ClientEngineFactory getInstance() {
		if (instance == null) {
			synchronized (ClientEngineFactory.class) {
				if (instance == null) {
					instance = new ClientEngineFactory();
				}
			}
		}
		
		return instance;
	}
	
	public ClientEngine get(String id) {
		return engineMap.get(id);
	}
	
	public void init(List<String> configFileList) {
		Map<String, ServiceConfigItem> serviceList = loadServiceConfig(configFileList);
		buildEngine(serviceList);
	}
	
	private Map<String, ServiceConfigItem> loadServiceConfig(List<String> configFiles) {
		Map<String, ServiceConfigItem> serviceList = new HashMap<String, ServiceConfigItem>();

		for (String configFile : configFiles) {
			logger.info(String.format("load service config from file %s", configFile));
			ServiceConfigLoader configLoader = new ServiceConfigLoader(configFile);
			Map<String, ServiceConfigItem> services = configLoader.load();
			for (Entry<String, ServiceConfigItem> entry : services.entrySet()) {
				if (serviceList.containsKey(entry.getKey())) {
					String errMessage = String.format("Duplicated serviceId %s found in service config file", entry.getKey());
					logger.error(errMessage);
					throw new RuntimeException(errMessage);
				}

				serviceList.put(entry.getKey(), entry.getValue());
			}
		}

		return serviceList;
	}

	private void buildEngine(Map<String, ServiceConfigItem> configItems) {
		if (configItems == null || configItems.size() == 0) {
			return;
		}

		for (ServiceConfigItem item : configItems.values()) {
			String serviceId = item.getServiceId();
			ClientEngine engine = create(item);
			engineMap.put(serviceId, engine);
		}
	}
	
	public ClientEngine create(ServiceConfigItem config) {
			try {
				logger.info(String.format("init service according to config: %s", config.toString()));
				ProtocolTypeEnum currentProtocol = config.getSupportedProtocol();
				if (currentProtocol == ProtocolTypeEnum.HTTP) {
					HttpClientEngine.checkParameter(config);
					HttpClientEngine httpClient = new HttpClientEngine(config);
					return httpClient;
				}
				else if (currentProtocol == ProtocolTypeEnum.HESSIAN) {
					HessianClientEngine.checkParameter(config);
					HessianClientEngine hessianClient = new HessianClientEngine(config);
					return hessianClient;
				}
				else if (currentProtocol == ProtocolTypeEnum.DUBBO) {
					throw new RuntimeException("DUBBO not support yet");
				}
			} catch(Exception ex) {
				logger.error(String.format("Fail to init service %s \n %s", config.getServiceId(), ex.getMessage()));
				throw new RuntimeException(ex.getMessage());
			}
			
			return null;
	}
}
