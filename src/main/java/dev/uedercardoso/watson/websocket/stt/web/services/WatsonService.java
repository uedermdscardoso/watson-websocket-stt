package dev.uedercardoso.watson.websocket.stt.web.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
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
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import dev.uedercardoso.watson.websocket.stt.web.domain.model.language.Language;

@Service
public class WatsonService {

	@Autowired
	IbmCloudService ibmCloudService;
	
	@Value("${ibm_cloud.speech_to_text.api_key}")
	String API_KEY;
	
	@Value("${ibm_cloud.speech_to_text.service_url}")
	String SERVICE_URL;
	
	public List<SpeechRecognitionAlternative> recognizeAudio(Language language, File audio) throws Exception {
		
		List<SpeechRecognitionAlternative> objects = new LinkedList<SpeechRecognitionAlternative>();
		
		try { 
			//String enconding = Base64.getEncoder().encodeToString(("apikey:"+API_KEY).getBytes("utf-8"));
			final String token = ibmCloudService.getBearerToken(API_KEY);
			
			if(!token.isEmpty()) {
				final JSONObject object = new JSONObject(token);

			  	if(object.has("access_token")) {
					
			  		/*String bearerToken = object.get("access_token").toString();

					Map<String, String> headers = new HashMap();
					//headers.put("Authorization", "Basic "+enconding);
					headers.put("apikey", API_KEY);
					headers.put("Authorization", "Bearer "+bearerToken);
					headers.put("X-Watson-Learning-Opt-Out", "false");
					headers.put("Content-Type", "audio/flac");
					headers.put("Cache-Control", "no-cache");
					headers.put("Content-Length", String.valueOf(audio.length()));
					headers.put("Accept", "*//*");
					headers.put("Accept-Encoding", "gzip, deflate, br");
					headers.put("Connection", "keep-alive");
					headers.put("Host", "api.us-east.speech-to-text.watson.cloud.ibm.com");

					Authenticator authenticator = new IamAuthenticator.Builder()
				      .apikey(API_KEY)
					  .url("https://iam.cloud.ibm.com/identity/token")
					  .clientId("apikey")
					  .clientSecret(API_KEY)
				      .disableSSLVerification(true)
				      .headers(headers)
				      .build();*/
					
					IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
					//authenticator.setHeaders(headers);
					
					SpeechToText speechToText = new SpeechToText(authenticator);
					speechToText.setServiceUrl(SERVICE_URL);
					
					RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
					    .audio(audio) // new FileInputStream()
					    .contentType(HttpMediaType.AUDIO_FLAC)
					    .model(getLanguage(language))
					    .wordAlternativesThreshold((float) 0.9)
					    .keywordsThreshold((float) 0.5)
					    .keywords(Arrays.asList("if", "world", "day"))
					    .maxAlternatives(3)
					    .build();
					
					SpeechRecognitionResults speechRecognitionResults = speechToText.recognize(recognizeOptions).execute().getResult();
					
					List<SpeechRecognitionResult> results = speechRecognitionResults.getResults();
					  
					if(!results.isEmpty()) {
					 results.forEach((result -> {
					  if(!result.getAlternatives().isEmpty()) {
						  SpeechRecognitionAlternative alternative = result.getAlternatives().get(0);
						  
						  objects.add(alternative);
					  }
					 }));
					}
				}
			}
		
		} catch (FileNotFoundException e) {
			throw new Exception(e);
		} catch(Exception e) {
			throw new Exception(e);
		}

		return objects;
	}
	
	private String getLanguage(Language language) {
		final String en_US = "en-US_BroadbandModel";
		
		switch(language.name()) {
			case "PT_BR":
				return "pt-BR_BroadbandModel";
			case "EN_US": 
				return en_US;
			case "ES_ES": 
				return "es-ES_BroadbandModel";
			default:
				return en_US;
		}
	}
}
