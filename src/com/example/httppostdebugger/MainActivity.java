package com.example.httppostdebugger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity {

	EditText searchQuery;
	Button postButton;
	EditText responseInfo;
	EditText queryToSend;
	CheckBox encodeSpaces;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		responseInfo = (EditText) findViewById(R.id.editText1);
		searchQuery = (EditText) findViewById(R.id.editText2);
		queryToSend = (EditText) findViewById(R.id.editText3);
		
		encodeSpaces = (CheckBox) findViewById(R.id.checkBox1);
		
		postButton = (Button) findViewById(R.id.button1);
		if (postButton != null) {
			postButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (searchQuery.getText() != null && searchQuery.getText().length() > 0) {	
						String query = searchQuery.getText().toString();
						responseInfo.setText("Sending POST...");
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
						new HttpPostAsyncTask().execute(query);
					} else {
						searchQuery.setHintTextColor(Color.parseColor("#FF0000"));
						searchQuery.setHint("PLEASE enter a search query");
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class HttpPostAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
//			String url = "http://posttestserver.com/post.php?dir=jmo";
			String url = "http://posttestserver.com/post3.php?dir=jmo";
			
			StringBuilder mobileQuery = new StringBuilder("?page=1&per_page=20&query=");
			mobileQuery.append(params[0]);
			String queryTranlsated;
			try {
				queryTranlsated = URLEncoder.encode(mobileQuery.toString(), HTTP.UTF_8);
				if (encodeSpaces.isChecked()) {
					queryTranlsated = queryTranlsated.replace("+", "%20");
				}
				
				final String updateQueryString = queryTranlsated;
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						queryToSend.setText(updateQueryString);
					}
				});
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return "Error encoding query";
			}
			
			StringBuilder id = new StringBuilder();
			char[] chars = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ0123456789".toCharArray();
			Random random = new Random();
			for (int i = 0; i < 20; i++) {
			    char c = chars[random.nextInt(chars.length)];
			    id.append(c);
			}
			
			String name = "test";
			
			List<NameValuePair> values = new ArrayList<NameValuePair>();;
			values.add(new BasicNameValuePair("search[name]", name));
			values.add(new BasicNameValuePair("search[id]", id.toString()));
			values.add(new BasicNameValuePair("search[mobile_query]", queryTranlsated));
			
			return post(url, values, false);
		}
		
		@Override
		protected void onPostExecute(String response) {
			if (responseInfo != null) {
				responseInfo.setText(response);
			}
		}
	}

	public static String post(String url, List<NameValuePair> values, boolean timeout) {		
		byte[] bytes = null;
		StringBuilder responseString = new StringBuilder();
		try {
			HttpClient httpClient = new DefaultHttpClient();
			if (timeout) {
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 10000);
				HttpConnectionParams.setSoTimeout(params, 15000);
				httpClient = new DefaultHttpClient(params);
			}
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Accept", "*/*");
            UrlEncodedFormEntity encodedValues = new UrlEncodedFormEntity(values, HTTP.UTF_8);
	        httpPost.setEntity(encodedValues);
	        HttpResponse httpResponse = httpClient.execute(httpPost);

            int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
            responseString.append("Status Code: ");
            responseString.append(httpStatusCode); 
            responseString.append("\nResponse:\n");
            responseString.append(httpResponse.getStatusLine().getReasonPhrase().toString() + "\n");
            if (httpStatusCode < 200 || httpStatusCode > 299) {
                throw new Exception("Error posting to URL: " + url + " due to " + httpResponse.getStatusLine().getReasonPhrase());
            }
	        	
	        HttpEntity httpEntity = httpResponse.getEntity();
	        bytes = toBytes(httpEntity.getContent());
		} catch (Exception ex) {
			Log.e("NetworkManager.post", ex.getMessage());
			responseString.append("Error: " + ex.getLocalizedMessage());
		} finally {
			Log.w("NetworkManager.post", url);
		}
		return responseString.toString();
	}
	
	private static byte[] toBytes(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();                  
        int bytesRead;                 
        byte[] buffer = new byte[1024];                 
        while ((bytesRead = is.read(buffer)) != -1) {                         
        	os.write(buffer, 0, bytesRead);                 
        } 
        return os.toByteArray();
	}
}
