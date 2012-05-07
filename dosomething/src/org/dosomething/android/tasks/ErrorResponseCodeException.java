package org.dosomething.android.tasks;


public class ErrorResponseCodeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private int responseCode;
	private String url;
	

	public ErrorResponseCodeException(int responseCode, String url){
		super(String.format("Received response code %d from %s", responseCode, url));
		this.responseCode = responseCode;
		this.url = url;
	}


	public int getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}

}
