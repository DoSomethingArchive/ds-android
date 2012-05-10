package org.dosomething.android.tasks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.context.SessionContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public abstract class AbstractWebserviceTask extends AsyncTask<Void,Void,Boolean>{


	private static final String TAG = "AbstractWebserviceTask";
	private static final String ACCEPT_GZIP = "gzip";
	private static final String UA = "android";

	protected static final String API_URL = "http://apps.dosomething.org/m_app_api/";

	protected abstract void onSuccess();
	protected abstract void onFinish();
	protected abstract void onError();

	protected abstract void doWebOperation() throws Exception;
	
	private final SessionContext sessionContext;
	
	public AbstractWebserviceTask(SessionContext sessionContext){
		this.sessionContext = sessionContext;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean result = false;

		try{
			doWebOperation();
			result = true;
		}catch(Exception e){
			Log.e(TAG, "Unable to call webservice", e);
		}

		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try{
			if(result){
				onSuccess();
			}else{
				onError();
			}
		}finally{
			onFinish();
		}
	}

	public WebserviceResponse doPost(String url, JSONObject json) throws IOException, JSONException{
		return doInputRequest(new HttpPost(url), json);
	}
	
	public WebserviceResponse doPost(String url, Map<String,String> params) throws IOException, JSONException{
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for(Entry<String,String> entry : params.entrySet()) {
			pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		StringEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		
		return doPost(url, entity, "application/x-www-form-urlencoded");
	}
	
	public WebserviceResponse doPost(String url, String params) throws IOException, JSONException{
		StringEntity entity = new StringEntity(params, "UTF-8");
		
		return doPost(url, entity, "application/json");
	}
	
	public WebserviceResponse doPost(String url, StringEntity entity, String contentType) throws IOException, JSONException{
		HttpEntityEnclosingRequestBase request = new HttpPost(url);

		HttpClient client = new DefaultHttpClient();

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("Content-type", contentType);
		request.addHeader("User-Agent", UA);

		request.setEntity(entity);

		HttpResponse response = client.execute(request, sessionContext.getHttpContext());

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

		String requestString = json.toString();

		request.setEntity(new StringEntity(requestString, "UTF-8"));

		HttpResponse response = client.execute(request, sessionContext.getHttpContext());

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

	public WebserviceResponse doGet(String url) throws IOException, JSONException{
		HttpClient client = new DefaultHttpClient();

		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("User-Agent", UA);

		HttpResponse response = client.execute(request, sessionContext.getHttpContext());

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

	public void doDelete(String url) throws IOException{
		HttpClient client = new DefaultHttpClient();

			HttpDelete request = new HttpDelete(url);
			request.addHeader("Accept", "application/json");
			request.addHeader("Accept-Encoding", ACCEPT_GZIP);
			request.addHeader("Content-type", "application/json");
			request.addHeader("User-Agent", UA);

			HttpResponse response = client.execute(request, sessionContext.getHttpContext());

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
			if(body!=null && body.length() > 0){
				answer = new JSONObject(body);
			}
			
			return answer;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public InputStream getBody() {
			return body;
		}
	}
}
