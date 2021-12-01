package dev.uedercardoso.watson.websocket.stt.web.services;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
public class IbmCloudService {

	String getBearerToken(String apikey) throws Exception {
		try {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpPost = new HttpPost("https://iam.cloud.ibm.com/identity/token");
		
			final Set<BasicNameValuePair> parameters = new HashSet<BasicNameValuePair>();
			parameters.add(new BasicNameValuePair("grant_type", "urn:ibm:params:oauth:grant-type:apikey"));
			parameters.add(new BasicNameValuePair("apikey", apikey));
			
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
			
		    HttpResponse response = httpClient.execute(httpPost);

			final HttpEntity respEntity = response.getEntity();
			final String content =  EntityUtils.toString(respEntity);
			
			return content;
			
		} catch(Exception e) {
			throw new Exception(e);
		}
	}
	
}
