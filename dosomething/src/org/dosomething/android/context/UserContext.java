package org.dosomething.android.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;

import com.google.inject.Inject;

import org.dosomething.android.DSConstants;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserContext {
	
	private static final String MY_PREFS = "my_prefs";
	private static final String FIRST_NAME = "first_name";
	private static final String LAST_NAME = "last_name";
	private static final String ADDR_1 = "addr_1";
	private static final String ADDR_2 = "addr_2";
	private static final String ADDR_CITY = "addr_city";
	private static final String ADDR_STATE = "addr_state";
	private static final String ADDR_ZIP = "addr_zip";
	private static final String CREATED_TIMESTAMP = "created_timestamp";
	private static final String EMAIL = "email";
	private static final String EXPIRES_AT = "expires_at";
	private static final String FTAFS_SENT = "ftafs_sent";
	private static final String SESSION_ID = "session_id";
	private static final String SESSION_NAME = "session_name";
	private static final String SMS_CAMPAIGNS_STARTED = "sms_campaigns_started";
	private static final String UID = "user_uid";
	private static final String USER_NAME = "user_name";
	
	private static final long EXPIRES_AT_PADDING = 24 * 60 * 60 * 1000; // 24hrs
	
	private final Context context;
	
	@Inject
	public UserContext(Context context){
		this.context = context;
	}
	
	public String getUserName(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(USER_NAME, null);
		return uid;
	}
	
	public String getEmail(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(EMAIL, null);
		return uid;
	}
	
	public int getFtafsSent() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		int ftafsSent = settings.getInt(FTAFS_SENT, 0);
		return ftafsSent;
	}
	
	public String getUserUid(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(UID, null);
		return uid;
	}
	
	public String getSessionId(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(SESSION_ID, null);
		return uid;
	}
	
	public String getSessionName(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(SESSION_NAME, null);
		return uid;
	}
	
	public int getSmsCampaignsStarted() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		return settings.getInt(SMS_CAMPAIGNS_STARTED, 0);
	}
	
	public String getPhoneNumber() {
		TelephonyManager tmgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String phone = tmgr.getLine1Number();
		return phone;
	}
	
	public String getFirstName() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String firstName = settings.getString(FIRST_NAME, null);
		return firstName;
	}
	
	public String getLastName() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String lastName = settings.getString(LAST_NAME, null);
		return lastName;
	}
	
	public String getAddr1() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String addr1 = settings.getString(ADDR_1, null);
		return addr1;
	}
	
	public String getAddr2() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String addr2 = settings.getString(ADDR_2, null);
		return addr2;
	}
	
	public String getAddrCity() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String addrCity = settings.getString(ADDR_CITY, null);
		return addrCity;
	}
	
	public String getAddrState() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String addrState = settings.getString(ADDR_STATE, null);
		return addrState;
	}
	
	public String getAddrZip() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String addrZip = settings.getString(ADDR_ZIP, null);
		return addrZip;
	}
	
	public String getCreatedTime() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		long lCreatedTime = settings.getLong(CREATED_TIMESTAMP, 0);
		if (lCreatedTime > 0) {
			// Multiply by 1000 because the time retrieved from backend is unix and Date here expects milliseconds
			Date createdDate = new Date(lCreatedTime * 1000);
			return new SimpleDateFormat(DSConstants.DATE_FORMAT, Locale.US).format(createdDate);
		}
		else {
			return null;
		}
	}
	
	public boolean isLoggedIn(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(UID, null);
		long expires_at = settings.getLong(EXPIRES_AT, 0);
		long deadline = System.currentTimeMillis() + EXPIRES_AT_PADDING;
		
		return (uid != null && expires_at != 0 && expires_at > deadline);
	}
	
	public void setFirstName(String firstName) {
		if (firstName == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(FIRST_NAME, firstName);
		editor.commit();
	}
	
	public void setLastName(String lastName) {
		if (lastName == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(LAST_NAME, lastName);
		editor.commit();
	}
	
	public void setAddr1(String addr1) {
		if (addr1 == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(ADDR_1, addr1);
		editor.commit();
	}
	
	public void setAddr2(String addr2) {
		if (addr2 == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(ADDR_2, addr2);
		editor.commit();
	}
	
	public void setAddrCity(String addrCity) {
		if (addrCity == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(ADDR_CITY, addrCity);
		editor.commit();
	}
	
	public void setAddrState(String addrState) {
		if (addrState == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(ADDR_STATE, addrState);
		editor.commit();
	}
	
	public void setAddrZip(String addrZip) {
		if (addrZip == null) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(ADDR_ZIP, addrZip);
		editor.commit();
	}
	
	public void setCreatedTime(String createdTimestamp) {
		long lCreatedTime = Integer.valueOf(createdTimestamp);
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putLong(CREATED_TIMESTAMP, lCreatedTime);
		editor.commit();
	}
	
	public void setEmail(String email) {
		if (email == null || email.length() == 0) {
			return;
		}
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(EMAIL, email);
		editor.commit();
	}
	
	public void addFtafsSent(int additionalFtafs) {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		int ftafsSent = settings.getInt(FTAFS_SENT, 0);
		if (additionalFtafs > 0) {
			ftafsSent += additionalFtafs;
		}
		
		Editor editor = settings.edit();
		editor.putInt(FTAFS_SENT, ftafsSent);
		editor.commit();
	}
	
	public void addSmsCampaignsStarted() {
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		int numStarted = settings.getInt(SMS_CAMPAIGNS_STARTED, 0);
		numStarted++;

		Editor editor = settings.edit();
		editor.putInt(SMS_CAMPAIGNS_STARTED, numStarted);
		editor.commit();
	}
	
	public void setLoggedIn(String userName, String email, String uid, String sessionId, String sessionName, long expiresMins){
		
		long expiresAt = System.currentTimeMillis() + (expiresMins * 60 * 1000);
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(USER_NAME, userName);
		editor.putString(EMAIL, email);
		editor.putString(UID, uid);
		editor.putString(SESSION_ID, sessionId);
		editor.putString(SESSION_NAME, sessionName);
		editor.putLong(EXPIRES_AT, expiresAt);
		editor.commit();
	}

    /**
     * Takes json object returned from a successful login/registration and updates the user
     * context with the contained info.
     *
     * @param obj JSONObject of user and profile data
     */
    public void updateWithUserObject(JSONObject obj) throws Exception {
        JSONObject user = obj.getJSONObject("user");

        if (user != null && obj != null) {
            setLoggedIn(
                    user.optString("name", ""),
                    user.optString("mail", ""),
                    user.getString("uid"),
                    obj.getString("sessid"),
                    obj.getString("session_name"),
                    obj.getLong("session_cache_expire"));

            setCreatedTime(user.getString("created"));
        }

        JSONObject profile = obj.optJSONObject("profile");
        if (profile != null) {

            String firstName;
            if (profile.optJSONObject("field_user_first_name") != null
                    && profile.optJSONObject("field_user_first_name").optJSONArray("und") != null
                    && profile.optJSONObject("field_user_first_name").optJSONArray("und").optJSONObject(0) != null
                    && (firstName = profile.optJSONObject("field_user_first_name").optJSONArray("und").getJSONObject(0).optString("value", null)) != null)
            {
                setFirstName(firstName);
            }

            String lastName;
            if (profile.optJSONObject("field_user_last_name") != null
                    && profile.optJSONObject("field_user_last_name").optJSONArray("und") != null
                    && profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0) != null
                    && (lastName = profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0).optString("value", null)) != null)
            {
                setLastName(lastName);
            }


            JSONObject address;
            if (profile.optJSONObject("field_user_address") != null
                    && profile.optJSONObject("field_user_address").optJSONArray("und") != null
                    && (address = profile.optJSONObject("field_user_address").optJSONArray("und").optJSONObject(0)) != null)
            {
                String addr1 = address.optString("thoroughfare");
                String addr2 = address.optString("premise");
                String city = address.optString("locality");
                String state = address.optString("administrative_area");
                String zip = address.optString("postal_code");

                setAddr1(addr1);
                setAddr2(addr2);
                setAddrCity(city);
                setAddrState(state);
                setAddrZip(zip);
            }
        }
    }
	
	public void clear(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.remove(USER_NAME);
		editor.remove(EMAIL);
		editor.remove(UID);
		editor.remove(SESSION_ID);
		editor.remove(SESSION_NAME);
		editor.remove(EXPIRES_AT);
		editor.remove(FIRST_NAME);
		editor.remove(LAST_NAME);
		editor.remove(ADDR_1);
		editor.remove(ADDR_2);
		editor.remove(ADDR_CITY);
		editor.remove(ADDR_STATE);
		editor.remove(ADDR_ZIP);
		editor.commit();
	}
}
