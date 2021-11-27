package dev.uedercardoso.watson.websocket.stt.web.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.http.ServiceCallback;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.RecognizeWithWebsocketsOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

@Service
public class WatsonService {

	String API_KEY = "zJ6JAQGHuaiGl2_cWApVsQs2FRXUro0zXh6Z1lcwUdgX";
	String SERVICE_URL = "https://api.us-east.speech-to-text.watson.cloud.ibm.com/instances/6b204593-34da-420b-be3b-e5e7750697ee/v1/recognize?timestamps=true";
	
	String getBearerToken() throws Exception {
		try {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpPost = new HttpPost("https://iam.cloud.ibm.com/identity/token");
		
			final Set<BasicNameValuePair> parameters = new HashSet<BasicNameValuePair>();
			parameters.add(new BasicNameValuePair("grant_type", "urn:ibm:params:oauth:grant-type:apikey"));
			parameters.add(new BasicNameValuePair("apikey", API_KEY));
			
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
	
	public List<String> recognizeAudio(File audio) throws Exception {
		
		List<String> messages = new LinkedList<String>();
	  	
		try { 
			String enconding = Base64.getEncoder().encodeToString(("apikey:"+API_KEY).getBytes("utf-8"));
			
			final String token = getBearerToken();
			
			if(!token.isEmpty()) {
				final JSONObject object = new JSONObject(token);

			  	if(object.has("access_token")) {
					
					String bearerToken = object.get("access_token").toString();
				
					Map<String, String> headers = new HashMap();
					//headers.put("Authorization", "Basic "+enconding);
					headers.put("Authorization", "Bearer "+bearerToken);
					headers.put("Content-Type", "audio/flac");
					headers.put("Cache-Control", "no-cache");
					headers.put("Content-Length", String.valueOf(audio.length()));
					headers.put("Accept", "*/*");
					headers.put("Accept-Encoding", "gzip, deflate, br");
					headers.put("Connection", "keep-alive");
					headers.put("Host", "api.us-east.speech-to-text.watson.cloud.ibm.com");
					
					/*Authenticator authenticator = new IamAuthenticator.Builder()
				      .apikey(API_KEY)
					  .url(SERVICE_URL)
					  .headers(headers)
					  .build();*/
					
					IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
					authenticator.setHeaders(headers);
					
					SpeechToText speechToText = new SpeechToText(authenticator);
					speechToText.setServiceUrl(SERVICE_URL);
					
					RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
					    .audio(new FileInputStream(audio.getAbsolutePath()))
					    .contentType(HttpMediaType.AUDIO_FLAC)
					    .model("en-US_BroadbandModel")
					    .wordAlternativesThreshold((float) 0.9)
					    .keywordsThreshold((float) 0.5)
					    //.keywords(Arrays.asList("if", "world", "day"))
					    //.maxAlternatives(3)
					    .build();
					
					SpeechRecognitionResults speechRecognitionResults = speechToText.recognize(recognizeOptions).execute().getResult();
				
					List<SpeechRecognitionResult> results = speechRecognitionResults.getResults();
					  
					if(!results.isEmpty()) {
					 results.forEach((result -> {
					  if(!result.getAlternatives().isEmpty()) {
						  SpeechRecognitionAlternative alternative = result.getAlternatives().get(0);
						  
						  messages.add(alternative.getTranscript());
					  }
					 }));
					}
				}
			}
		
		} catch (FileNotFoundException e) {
			System.out.print("file not found: "+e.getMessage());
		} catch(Exception e) {
			throw new Exception(e);
			//System.out.print("exception: "+e.getMessage());
		}

		return messages;
	}
}
