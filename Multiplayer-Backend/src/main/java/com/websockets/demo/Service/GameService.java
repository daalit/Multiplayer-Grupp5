package com.websockets.demo.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.websockets.demo.Message;

@Service
public class GameService {

    private static final int gridSize = 15;
    private static final int roundMs = 30000; // 30 sekunder i millisekunder

    private final String[][] grid = new String[gridSize][gridSize]; // Varje cell innehåller en fräg (String) eller null
                                                                    // om rutan är rom:
    private String phase = "lobby";
    private Long roundEndsAt = null;

    @Autowired
    private MessagingService messagingService;

    // Starta en ny spelrunda
    public void startGame() {
        // kolla om vi kan starta spelet
        if ("running".equals(phase)) {
            return; // redan igång
        }

        // Återställ spelplanen
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                grid[r][c] = null;
            }
        }

        messagingService.broadcast(Map.of(
                "type", "gridReset",
                "grid", grid));

        // uppdatera status
        phase = "running";
        roundEndsAt = System.currentTimeMillis() + roundMs; // nuvarande klockslag i millisekunder

        // Skicka startmeddelande till klienten // GameSerivce kan inte själv direkt
        // prata med WebSocket-anslutningen
        // där av behöver vi delegera den istället

        messagingService.broadcast(new Message("roundStart", Map.of(
                "roundEndsAt", roundEndsAt,
                "now", System.currentTimeMillis())));

        // Thread låter spelet köra i 30 sekunder innan vi kör metoden endGame -

        new Thread(() -> {
            try {
                Thread.sleep(roundMs);
                endGame();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

    }

    private void endGame() {
        phase = "ended";

        // TODO: Räkna vinnare baserat på antal rutor
    }

}
