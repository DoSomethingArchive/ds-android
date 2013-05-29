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
	
	public static enum CAMPAIGN_TYPE {
		CHANGE_A_MIND,
		DONATION,
		HELP_1_PERSON,
		IMPROVE_A_PLACE,
		MADE_BY_YOU,
		SHARE_FOR_GOOD,
		SMS
	}
	
	public static enum CAUSE_TAG {
		ANIMALS(29),
		BULLYING(28),
		DISASTERS(27),
		DISCRIMINATION(23),
		EDUCATION(25),
		ENVIRONMENT(20),
		POVERTY(21),
		HUMAN_RIGHTS(73),
		TROOPS(24),
		HEALTH(26),
		RELATIONSHIPS(22);
		
		private int intValue;
		
		private CAUSE_TAG(int val) {
			intValue = val;
		}
		
		public int getValue() {
			return intValue;
		}
	}
}
