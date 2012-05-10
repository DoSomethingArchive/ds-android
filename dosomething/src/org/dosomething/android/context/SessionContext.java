package org.dosomething.android.context;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class SessionContext {
	
	private HttpContext httpContext;
	
	public SessionContext(){
		init();
	}
	
	private void init(){
		this.httpContext = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		this.httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	public void clear(){
		init();
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

}
