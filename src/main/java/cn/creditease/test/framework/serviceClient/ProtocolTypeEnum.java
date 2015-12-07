package cn.creditease.test.framework.serviceClient;

public enum ProtocolTypeEnum {
	HTTP("http", "http client"),
	HESSIAN("hessian", "hessian client"),
	DUBBO("dubbo", "dubbo client");
	
	private final String protocol;
	private final String desc;
	
	private ProtocolTypeEnum(String protocol, String desc) {
		this.protocol = protocol;
		this.desc = desc;
	}
	
	public static ProtocolTypeEnum toEnum(String protocol) {
		for (ProtocolTypeEnum each : ProtocolTypeEnum.values()) {
			if (each.getProtocol().equalsIgnoreCase(protocol)) {
				return each;
			}
		}
		
		return null;
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public String getDesc() {
		return this.desc;
	}
}
