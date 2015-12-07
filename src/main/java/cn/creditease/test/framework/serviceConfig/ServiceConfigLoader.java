package cn.creditease.test.framework.serviceConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.GlobalProperty;
import cn.creditease.test.framework.serviceClient.ProtocolTypeEnum;

public class ServiceConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger(ServiceConfigLoader.class);
	
	private static final String Id = "id";
	private static final String URL = "url";
	private static final String InterfaceName = "interface";
	private static final String Method = "method";
	private static final String Group = "group";
	private static final String Version = "version";
	private static final String Desc = "desc";
	
	private static final Pattern pattern = Pattern.compile("(\\$\\{\\w+\\})");
	private static final GlobalProperty properties = GlobalProperty.getInstance();
			
	private String filePath;
	
	public ServiceConfigLoader(String  configFile) {
		this.filePath = configFile;
	}
	
	public Map<String, ServiceConfigItem> load() {
		try {
			SAXReader reader = new SAXReader();
			InputStream input = getClass().getClassLoader().getResourceAsStream(filePath);
			if (input == null) {
				throw new RuntimeException(String.format("Service config file [%s] not found", this.filePath));
			}
			
			Document doc = reader.read(input);
			Element root = doc.getRootElement();
			List<Element> serviceNodes = root.elements();
			if (serviceNodes == null || serviceNodes.size() == 0) {
				return null;
			}
			
			Map<String, ServiceConfigItem> services = new HashMap<String, ServiceConfigItem>();
			for (Element each : serviceNodes) {
				ServiceConfigItem item = readNode(each);
				services.put(item.getServiceId(), item);
			}
			
			return services;
		} catch (DocumentException e) {
			logger.error(String.format("Run into an error when parsing service config file %s \n", this.filePath) + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(String.format("Run into an error when parsing service config file %s \n", this.filePath) + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private ServiceConfigItem readNode(Element node) {
		if (node == null) {
			return null;
		}
		
		String protocol = node.getName();
		ProtocolTypeEnum protocolType = ProtocolTypeEnum.toEnum(protocol);
		if (protocolType == null) {
			throw new RuntimeException(String.format("service config is not valid in file %s", this.filePath));			
		}
		
		List<DefaultAttribute> attrs = node.attributes();
		if (attrs == null || attrs.size() == 0) {
			throw new RuntimeException(String.format("service config is not valid in file %s", this.filePath));
		}
		
		ServiceConfigItem item = new ServiceConfigItem();
		item.setSupportedProtocol(protocolType);
		for (DefaultAttribute each : attrs) {
			String attrName = each.getName();
			String attrValue = each.getValue();
			String afterReplacement = doReplacement(attrValue);

			if (Id.equalsIgnoreCase(attrName)) {
				item.setServiceId(afterReplacement);
			}
			else if (URL.equalsIgnoreCase(attrName)) {
				item.setUrl(afterReplacement);
			}
			else if (InterfaceName.equalsIgnoreCase(attrName)) {
				item.setInterfaceName(afterReplacement);
			}
			else if (Method.equalsIgnoreCase(attrName)) {
				item.setMethod(afterReplacement);
			}
			else if (Group.equalsIgnoreCase(attrName)) {
				item.setGroup(afterReplacement);
			}
			else if (Version.equalsIgnoreCase(attrName)) {
				item.setVersion(afterReplacement);
			}
			else if (Desc.equalsIgnoreCase(attrName)) {
				item.setDesc(afterReplacement);
			}
			else{
				logger.error(String.format("Skip attribute [%s=%s] setting in file %s", attrName, attrValue, this.filePath));
			}
		}
		
		return item;
	}
	
	private static String doReplacement(String srcStr) {
		Matcher m = pattern.matcher(srcStr);
		if (m.find()) {
			String matchedKey = m.group(0);
			String propertyKey = matchedKey.substring(2, matchedKey.length() - 1);
			String repacement = properties.getValue(propertyKey); 
			String newValue = m.replaceAll(repacement);
			return newValue;
		}
		
		return srcStr;
	}
}

