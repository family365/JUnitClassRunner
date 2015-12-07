package cn.creditease.test.framework.db;

public class SQLConnectionDTO {
	private String databaseType;
	private String driver;
	private String url;
	private String userName;
	private String password;
	
	public String getDatabaseType() {
		return databaseType;
	}
	
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	
	public String getDriver() {
		return driver;
	}
	
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
