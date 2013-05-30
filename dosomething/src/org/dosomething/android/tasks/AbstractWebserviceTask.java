package org.dosomething.android.tasks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dosomething.android.context.UserContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

public abstract class AbstractWebserviceTask extends AsyncTask<Void,Void,Exception>{


	private static final String TAG = "AbstractWebserviceTask";
	private static final String ACCEPT_GZIP = "gzip";
	private static final String UA = "android";

	protected abstract void onSuccess();
	protected abstract void onFinish();
	protected abstract void onError(Exception e);

	protected abstract void doWebOperation() throws Exception;
	
	private final UserContext userContext;
	
	public AbstractWebserviceTask(UserContext userContext){
		this.userContext = userContext;
	}

	@Override
	protected Exception doInBackground(Void... params) {
		Exception exception = null;

		try{
			doWebOperation();
		}catch(Exception e){
			Log.d(TAG, "Exception during webservice. ", e);
			exception = e;
		}

		return exception;
	}

	@Override
	protected void onPostExecute(Exception exception) {
		try{
			if(exception==null){
				onSuccess();
			}else{
				onError(exception);
			}
		}finally{
			onFinish();
		}
	}

	public WebserviceResponse doPost(String url, JSONObject json) throws IOException, JSONException{
		return doInputRequest(new HttpPost(url), json);
	}
	
	public WebserviceResponse doPost(String url, List<NameValuePair> params) throws IOException, JSONException{
		StringEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		
		return doPost(url, entity, "application/x-www-form-urlencoded");
	}
	
	public WebserviceResponse doPost(String url, String params) throws IOException, JSONException{
		StringEntity entity = new StringEntity(params, "UTF-8");
		
		return doPost(url, entity, "application/json");
	}
	
	public WebserviceResponse doPost(String url, StringEntity entity, String contentType) throws IOException, JSONException{
		
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
		
		HttpEntityEnclosingRequestBase request = new HttpPost(url);

		HttpClient client = new DefaultHttpClient();

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("Content-type", contentType);
		request.addHeader("User-Agent", UA);
		
		if(userContext.isLoggedIn()) {
			request.addHeader("Cookie", userContext.getSessionName()+"="+userContext.getSessionId());
		}
		
		request.setEntity(entity);

		HttpResponse response = client.execute(request);

		int responseCode = response.getStatusLine().getStatusCode();

		HttpEntity responseEntity = response.getEntity();

		InputStream is = responseEntity.getContent();

		Header encoding = responseEntity.getContentEncoding();

		if(encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())){
			is = new GZIPInputStream(is);
		}

		if(responseCode >= 500){
			throw new ErrorResponseCodeException(responseCode, request.getURI().toString());
		}

		return new WebserviceResponse(responseCode, is);
	}

	public WebserviceResponse doPut(String url, JSONObject json) throws IOException, JSONException{
		return doInputRequest(new HttpPut(url), json);
	}

	private WebserviceResponse doInputRequest(HttpEntityEnclosingRequestBase request, JSONObject json) throws IOException, JSONException{

		HttpClient client = new DefaultHttpClient();

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("Content-type", "application/json");
		request.addHeader("User-Agent", UA);
		
		if(userContext.isLoggedIn()) {
			request.addHeader("Cookie", userContext.getSessionName()+"="+userContext.getSessionId());
		}

		String requestString = json.toString();

		request.setEntity(new StringEntity(requestString, "UTF-8"));

		HttpResponse response = client.execute(request);

		int responseCode = response.getStatusLine().getStatusCode();

		HttpEntity responseEntity = response.getEntity();

		InputStream is = responseEntity.getContent();

		Header encoding = responseEntity.getContentEncoding();

		if(encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())){
			is = new GZIPInputStream(is);
		}

		if(responseCode >= 500){
			throw new ErrorResponseCodeException(responseCode, request.getURI().toString());
		}

		return new WebserviceResponse(responseCode, is);
	}
	
	public static boolean isOnline(Context context) { 
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();    
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	public static WebserviceResponse doGet(String url, UserContext userContext) throws IOException, JSONException{
		HttpClient client = new DefaultHttpClient();

		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("User-Agent", UA);

		if(userContext != null){
			if(userContext.isLoggedIn()) {
				request.addHeader("Cookie", userContext.getSessionName()+"="+userContext.getSessionId());
			}
		}
		
		HttpResponse response = client.execute(request);

		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode < 200 || responseCode >= 300){
			throw new ErrorResponseCodeException(responseCode, url);
		}

		HttpEntity responseEntity = response.getEntity();

		InputStream is = responseEntity.getContent();

		Header encoding = responseEntity.getContentEncoding();

		if(encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())){
			is = new GZIPInputStream(is);
		}

		return new WebserviceResponse(responseCode, is);
	}

	public WebserviceResponse doGet(String url) throws IOException, JSONException{
		return doGet(url, this.userContext);
	}

	public void doDelete(String url) throws IOException{
		HttpClient client = new DefaultHttpClient();

			HttpDelete request = new HttpDelete(url);
			request.addHeader("Accept", "application/json");
			request.addHeader("Accept-Encoding", ACCEPT_GZIP);
			request.addHeader("Content-type", "application/json");
			request.addHeader("User-Agent", UA);
			
			if(userContext.isLoggedIn()) {
				request.addHeader("Cookie", userContext.getSessionName()+"="+userContext.getSessionId());
			}
			
			HttpResponse response = client.execute(request);

			int responseCode = response.getStatusLine().getStatusCode();

			if(responseCode < 200 || responseCode >= 300){
				throw new ErrorResponseCodeException(responseCode, url);
			}
	}
	
	
	public static class WebserviceResponse {
		private int statusCode;
		private InputStream body;
		
		public WebserviceResponse(int statusCode, InputStream body) {
			this.statusCode = statusCode;
			this.body = body;
		}
		
		public String getBodyAsString() throws IOException {
			String answer = null;
			
			if(body!=null) {
				BufferedInputStream bis = new BufferedInputStream(body);
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				int result = bis.read();
				while(result != -1) {
					byte b = (byte)result;
					buf.write(b);
					result = bis.read();
				}
				answer = buf.toString("UTF-8");
			}
			
			return answer;
		}
		
		public JSONObject getBodyAsJSONObject() throws JSONException, IOException {
			JSONObject answer = null;

			String body = getBodyAsString();
			if(body!=null && body.length() > 0) {
				if(body.equals("null")) {
					Log.w(TAG, "Webservice response body = 'null'");
				} else {
					answer = new JSONObject(body);
				}
			}
			
			return answer;
		}
		
		public JSONArray getBodyAsJSONArray() throws JSONException, IOException {
			JSONArray answer = null;
			
			String body = getBodyAsString();
			if (body != null && body.length() > 0) {
				answer = new JSONArray(body);
			}
			
			return answer;
		}
		
		public String extractFormErrorsAsMessage() throws JSONException, IOException {
			JSONObject obj = getBodyAsJSONObject();
			if(obj==null) {
				return null;
			}
			
			JSONObject formErrors = obj.optJSONObject("form_errors");
			if(formErrors==null) {
				return null;
			}
			
			StringBuilder message = new StringBuilder();
			JSONArray names = formErrors.names();
			for(int i=0; i<names.length(); i++) {
				String htmlError = formErrors.getString(names.getString(i));
				String plainText = Html.fromHtml(htmlError).toString();
				message.append(plainText);
				if(i+1<names.length()) {
					message.append("\n");
				}
			}
			return message.toString();
		}

		public int getStatusCode() {
			return statusCode;
		}

		public InputStream getBody() {
			return body;
		}
		
		public boolean hasErrorStatusCode() {
			if (statusCode >= 400 && statusCode < 500)
				return true;
			else
				return false;
		}
	}
}
