package com.websockets.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    private final SimpMessagingTemplate template;

    @Autowired
    public MessagingService(SimpMessagingTemplate template) {
        this.template = template;
    }

    // skicka till alla klienter som Lyssnar på /topic/game
    public void broadcast(Object message) {
        template.convertAndSend("/topic/game", message);
    }

    public void broadcast(String topic, Object message) {
        template.convertAndSend(topic, message);
    }

}
