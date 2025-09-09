package com.websockets.demo.Service;


import com.websockets.demo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class GameService {

    private static final int gridSize = 15;
    private static final int roundMs = 30000; // 30 sekunder i millisekunder

    private final String[][] grid = new String[gridSize][gridSize]; // Varje cell innehåller en fräg (String) eller null om rutan är rom:
    private String phase = "lobby";
    private Long roundEndsAt = null;
    private boolean testMode = false; // används bara i tester

    @Autowired
    public MessagingService messagingService;

    // GETTER för tester
    public String getPhase() {
        return phase;
    }

    public Long getRoundEndsAt() {
        return roundEndsAt;
    }

    public String[][] getGrid() {
        return grid;
    }

    //  sätt testMode från tester
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }


    //Starta en ny spelrunda
    public void startGame() {
        //kolla om vi kan starta spelet
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
                "grid", grid
        ));

        //uppdatera status
        phase = "running";
        roundEndsAt = System.currentTimeMillis() + roundMs;  // nuvarande klockslag i millisekunder från 1970-01-01 - detta hjälper oss att veta när spelet ska avlutas från när de startat.

        // Skicka startmeddelande till klienten // GameSerivce kan inte själv direkt prata med WebSocket-anslutningen
        // där av behöver vi delegera den istället
        messagingService.broadcast(new Message("roundStart", Map.of(
                "roundEndsAt", roundEndsAt,
                "now", System.currentTimeMillis()
        )));


        // Thread --> låter spelet köra i 30 sekunder innan vi kör metoden endGame - Thread "sover" i 30 sekunder innan den kör endGame
       if (!testMode) {
           new Thread(() -> {
               try {
                   Thread.sleep(roundMs); // vänta 30 sekunder
                   endGame(); // kör metoden endGame();
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               }
           }).start();
       }
    }

    public void endGame() {
        phase = "ended";

        // TODO: Räkna vinnare baserat på antal rutor
    }

}
