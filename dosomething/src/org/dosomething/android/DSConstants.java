package org.dosomething.android;

public class DSConstants {

    // Class is non-instantiable
    private DSConstants() {}

    // Flag to enable/disable analytics tracking. Should be set to false during development to avoid dirtying the data.
    public static final boolean analyticsEnabled = false;

    // IDs and keys for web services
    public static final String FACEBOOK_APP_ID = "525191857506466";
    public static final String PICS_API_KEY = "aea12e3fe5f83f0d574fdff0342aba91";
    public static final String FLURRY_API_KEY = "P4R2NTE9XSRAHB9IR9M2";

    // API URLs
    public static String API_URL_BASE = "http://www.dosomething.org/rest/";
    public static final String API_URL_FBLOGIN = API_URL_BASE + "user/fblogin.json";
    public static final String API_URL_FILE = API_URL_BASE + "file.json";
    public static final String API_URL_LOGIN = API_URL_BASE + "user/login.json";
    public static final String API_URL_LOGOUT = API_URL_BASE + "user/logout.json";
    public static final String API_URL_PROFILE_UPDATE = API_URL_BASE + "profile/%d.json";
    public static String API_URL_USER_DELETE = API_URL_BASE + "user/%d.json";
    public static String API_URL_USER_REGISTER = API_URL_BASE + "user/register.json";
    public static final String API_URL_WEBFORM = API_URL_BASE + "webform.json";
    public static final String CAMPAIGN_API_URL = "http://apps.dosomething.org/m_app_api";
    public static final String MCOMMONS_API_JOIN_URL = "http://dosomething.mcommons.com/profiles/join";

    // Date format to use across all areas of the app that need it
    public static final String DATE_FORMAT = "M/d/yyyy";

    // Fade-in animation time (in milliseconds) for ImageLoader images
    public static final int IMAGE_LOADER_FADE_IN_TIME = 250;

    // Keys used for data passed to Activity's through Intent extra Bundles
    public static enum EXTRAS_KEY {
        CAMPAIGN("campaign"),
        SFGITEM("sfg-item"),
        SHOW_SUBMISSIONS("show-submissions"),
        TOAST_MSG("toast-msg");

        private String value;

        private EXTRAS_KEY(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }

    // Specifies a campaign's type
    public static enum CAMPAIGN_TYPE {
        CHANGE_A_MIND,
        DONATION,
        HELP_1_PERSON,
        IMPROVE_A_PLACE,
        MADE_BY_YOU,
        SHARE_FOR_GOOD,
        SMS
    }

    // Causes and their int values as provided by DoSomething.org action finder circa 2011
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

        private int value;

        private CAUSE_TAG(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }
}
