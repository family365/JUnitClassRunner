package cn.creditease.test.framework.exception;

public class EngineInitException extends RuntimeException {
	private String errCode;
	private String errMessage;
	
	public EngineInitException(String errCode, String errMessage) {
		super(errMessage);
		this.errCode = errCode;
		this.errMessage = errMessage;
	}
	
	public EngineInitException(String errMessage, Throwable cause) {
		super(errMessage, cause);
		this.errMessage = errMessage;
	}
	
	public EngineInitException(Throwable cause) {
		super(cause);
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}
}
