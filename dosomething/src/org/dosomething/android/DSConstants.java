package org.dosomething.android;

public class DSConstants {

	// Class is non-instantiable
	private DSConstants() {}
	
	public static final String API_URL_BASE = "https://www.dosomething.org/rest/";
	public static final String API_URL_FBLOGIN = API_URL_BASE + "user/fblogin.json";
	public static final String API_URL_FILE = API_URL_BASE + "file.json";
	public static final String API_URL_LOGIN = API_URL_BASE + "user/login.json";
	public static final String API_URL_LOGOUT = API_URL_BASE + "user/logout.json";
	public static final String API_URL_USER_REGISTER = API_URL_BASE + "user/register.json";
	public static final String API_URL_WEBFORM = API_URL_BASE + "webform.json";
	
	public static final String CAMPAIGN_API_URL = "http://apps.dosomething.org/m_app_api";
	
	public static final String DATE_FORMAT = "MM/dd/yyyy";
}
