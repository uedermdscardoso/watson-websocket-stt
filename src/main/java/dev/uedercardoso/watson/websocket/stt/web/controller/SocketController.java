package dev.uedercardoso.watson.websocket.stt.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;

import dev.uedercardoso.watson.websocket.stt.web.domain.model.language.Language;
import dev.uedercardoso.watson.websocket.stt.web.services.WatsonService;

@Controller
@RequestMapping("/recognize")
public class SocketController {

	//@Autowired
    //SimpMessagingTemplate template;

    @Autowired
    WatsonService watsonService;
    
    @Autowired
    private ResourceLoader resourceLoader;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @GetMapping("/ok")
    public ResponseEntity<String> teste() {
    	return ResponseEntity.ok("CERTO");
    }
    
    @PostMapping("/media/{language}")
    public ResponseEntity<List<SpeechRecognitionAlternative>> sendMessage(@PathVariable String language, @RequestParam MultipartFile audio) throws Exception {
        
		try {
			
			if(Language.valueOf(language.toUpperCase()).name().isEmpty())
				throw new Exception("Language not found");
			
			final UUID id = UUID.randomUUID();
			FileUtils.writeByteArrayToFile(new File("temp_file_"+id), audio.getBytes());
			final File media = new File("temp_file_"+id);
			
			List<SpeechRecognitionAlternative> objects = watsonService.recognizeAudio(Language.valueOf(language.toUpperCase()), media);
		
			return ResponseEntity.ok(objects);
			
		} catch (IOException e) {
			return ResponseEntity.badRequest().build();
		} catch(Exception e) {
			return ResponseEntity.badRequest().build();	
		}
    }
    
    /*@MessageMapping("/hello")
    public void greeting() {
        
    	final Resource fileResource = resourceLoader.getResource("classpath:8TzgE5KgzVXVRElsLSwd.flac");
    	
    	File audio;
    	
		try {
			audio = fileResource.getFile();
	    	
			scheduler.scheduleAtFixedRate(() -> {
	            template.convertAndSend("/topic/message", watsonService.recognizeAudio(audio));
	        }, 0, 2, TimeUnit.SECONDS);	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	/*scheduler.scheduleAtFixedRate(() -> {
            template.convertAndSend("/topic/message", watsonService.getMessage());
        }, 0, 2, TimeUnit.SECONDS);
        
    }*/
	
}
