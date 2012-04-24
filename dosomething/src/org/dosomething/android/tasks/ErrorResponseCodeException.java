package org.dosomething.android.tasks;


public class ErrorResponseCodeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ErrorResponseCodeException(int responseCode, String url){
		super(String.format("Received response code %d from %s", responseCode, url));
	}

}
