package com.websockets.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class StompController {

    private final List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
    private final Map<String, String> playerColors = new HashMap<>();
    private final Map<String, Integer> scores = new HashMap<>();
    private final String[][] grid = new String[15][15]; // sparar vilken färg varje cell har

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public PlayerAssignment join(String sessionId) {
        if (!playerColors.containsKey(sessionId) && playerColors.size() < colors.size()) {
            String color = colors.get(playerColors.size());
            playerColors.put(sessionId, color);
            scores.put(color, 0);
        }
        return new PlayerAssignment(sessionId, playerColors.get(sessionId));
    }

    @MessageMapping("/grid")
    @SendTo("/topic/grid")
    public GridMessage handleGrid(GridMessage message) {
        int row = message.getRow();
        int col = message.getCol();
        String newColor = message.getColor();
        String oldColor = grid[row][col];

        // Uppdatera poäng
        if (oldColor != null && scores.containsKey(oldColor)) {
            scores.put(oldColor, scores.get(oldColor) - 1);
        }
        if (newColor != null && scores.containsKey(newColor)) {
            scores.put(newColor, scores.get(newColor) + 1);
        }

        grid[row][col] = newColor;

        return message; // skickas till alla som grid update
    }

    @MessageMapping("/scores")
    @SendTo("/topic/scores")
    public ScoreUpdate getScores() {
        return new ScoreUpdate(scores);
    }
}
