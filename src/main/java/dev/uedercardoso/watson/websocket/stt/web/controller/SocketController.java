package dev.uedercardoso.watson.websocket.stt.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    @Value("classpath:8TzgE5KgzVXVRElsLSwd.flac")
    private Resource fileResource;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @GetMapping("/media")
    public ResponseEntity<List<String>> sendMessage() throws Exception {
        
    	//final Resource fileResource //resourceLoader.getResource("classpath:8TzgE5KgzVXVRElsLSwd.flac");
    	
    	File audio;
    	
		try {
			audio = fileResource.getFile();
	    	
			List<String> messages = watsonService.recognizeAudio(audio);
		
			return ResponseEntity.ok(messages);
			
		} catch (IOException e) {
			throw new Exception(e);
			//return ResponseEntity.badRequest().build();
		} catch(Exception e) {
			throw new Exception(e);
			//return ResponseEntity.badRequest().build();	
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
