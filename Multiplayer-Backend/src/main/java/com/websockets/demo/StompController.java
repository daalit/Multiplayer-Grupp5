package com.websockets.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class StompController {

    private final List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
    private final Map<String, String> playerColors = new HashMap<>();

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public PlayerAssignment join(String sessionId) {
        if (!playerColors.containsKey(sessionId) && playerColors.size() < colors.size()) {
            String color = colors.get(playerColors.size());
            playerColors.put(sessionId, color);
        }
        return new PlayerAssignment(sessionId, playerColors.get(sessionId));
    }

    @MessageMapping("/grid")
    @SendTo("/topic/grid")
    public GridMessage handleGrid(GridMessage message) {
        return message;
    }
}
