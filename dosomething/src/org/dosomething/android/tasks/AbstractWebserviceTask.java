package org.dosomething.android.tasks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public abstract class AbstractWebserviceTask extends AsyncTask<Void,Void,Boolean>{

	private static final String TAG = "AbstractWebserviceTask";
	private static final String ACCEPT_GZIP = "gzip";
	private static final String UA = "android";

	protected static final String URL = "";

	protected abstract void onSuccess();
	protected abstract void onFinish();
	protected abstract void onError();

	protected abstract void doWebOperation() throws Exception;

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

	public static JSONObject getObject(String url) throws JSONException, IOException{
		return new JSONObject(doGet(url));
	}

	public static JSONArray getArray(String url) throws JSONException, IOException{
		String responseString = doGet(url);

		if(responseString != null && responseString.length() > 0){
			return new JSONArray(responseString);
		}else{
			return new JSONArray();
		}
	}

	public static JSONObject doPost(String url, JSONObject json) throws IOException, JSONException{
		return doInputRequest(new HttpPost(url), json);
	}

	public static JSONObject doPost(String url, String params) throws IOException, JSONException{
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		JSONObject answer;

		HttpClient client = new DefaultHttpClient();

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("Content-type", "application/json");
		request.addHeader("User-Agent", UA);

		String requestString = params;

		request.setEntity(new StringEntity(requestString, "UTF-8"));

		HttpResponse response = client.execute(request);

		int responseCode = response.getStatusLine().getStatusCode();

		HttpEntity responseEntity = response.getEntity();

		InputStream is = responseEntity.getContent();

		Header encoding = responseEntity.getContentEncoding();

		if(encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())){
			is = new GZIPInputStream(is);
		}

		String responseString = toString(is);

		if(responseCode < 200 || responseCode >= 300){
			throw new RuntimeException(url + " Got non 200 status: " + responseCode + ".  Response: " + responseString);
		}

		if(responseString != null && responseString.length() > 0){
			answer = new JSONObject(responseString);
		}else{
			answer = null;
		}

		return answer;
	}

	public static JSONObject doPut(String url, JSONObject json) throws IOException, JSONException{
		return doInputRequest(new HttpPut(url), json);
	}

	private static JSONObject doInputRequest(HttpEntityEnclosingRequestBase request, JSONObject json) throws IOException, JSONException{
		JSONObject answer;

		HttpClient client = new DefaultHttpClient();

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("Content-type", "application/json");
		request.addHeader("User-Agent", UA);

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

		String responseString = toString(is);

		if(responseCode < 200 || responseCode >= 300){
			throw new ErrorResponseCodeException(responseCode, request.getURI().toString());
		}

		if(responseString != null && responseString.length() > 0){
			answer = new JSONObject(responseString);
		}else{
			answer = null;
		}

		return answer;
	}

	public static String doGet(String url) throws IOException, JSONException{
		String answer;

		HttpClient client = new DefaultHttpClient();

		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", ACCEPT_GZIP);
		request.addHeader("User-Agent", UA);

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

		answer = toString(is);

		return answer;
	}

	public static void doDelete(String url) throws IOException{
		HttpClient client = new DefaultHttpClient();

			HttpDelete request = new HttpDelete(url);
			request.addHeader("Accept", "application/json");
			request.addHeader("Accept-Encoding", ACCEPT_GZIP);
			request.addHeader("Content-type", "application/json");
			request.addHeader("User-Agent", UA);

			HttpResponse response = client.execute(request);

			int responseCode = response.getStatusLine().getStatusCode();

			if(responseCode < 200 || responseCode >= 300){
				throw new ErrorResponseCodeException(responseCode, url);
			}
	}

	public static String toString(InputStream in) 
			throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
			byte b = (byte)result;
			buf.write(b);
			result = bis.read();
		}       
		return buf.toString("UTF-8");
	}
}
