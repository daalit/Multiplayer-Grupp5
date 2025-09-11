package com.websockets.demo.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import com.websockets.demo.Message;

@Service
public class GameService {

    private static final int gridSize = 15;
    private static final int roundMs = 30000; // 30 sekunder i millisekunder
    private boolean testMode = false; // används bara i tester

    @Autowired
    public MessagingService messagingService;

    @Autowired
    private GameManager gameManager;



    // Starta en ny spelrunda
    public void startGame(String gameId) {
        GameSession session = gameManager.getSession(gameId);
        if (session == null) {
            System.out.println("Ingen session hittades för gameId: " + gameId);
            return;
        }


        // Återställ spelplanen
        String[][] grid = session.getGrid();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                grid[r][c] = null;
            }
        }

        session.setPhase("running");
        session.setRoundEndsAt(System.currentTimeMillis() + roundMs);

        messagingService.broadcast("/topic/game/" + gameId, Map.of(
                "type", "gridReset",
                "grid", grid));


        // Skicka startmeddelande till klienten // GameSerivce kan inte själv direkt
        // prata med WebSocket-anslutningen
        // där av behöver vi delegera den istället
        messagingService.broadcast("/topic/game/" + gameId, Map.of(
                "type", "roundStart",
                "roundEndsAt", session.getRoundEndsAt()));

        // Thread låter spelet köra i 30 sekunder innan vi kör metoden endGame -
       if (!testMode) {
           new Thread(() -> {
               try {
                   Thread.sleep(roundMs); // vänta 30 sekunder
                   endGame(gameId);
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               }
           }).start();
       }

    }


    // Vi räknar poäng när spelet är avslutat
    public void endGame(String gameId) {
        GameSession session = gameManager.getSession(gameId);
        if (session == null) return;

        System.out.println(">>> endGame() körs nu!");

        session.setPhase("ended");
        String[][] grid = session.getGrid();


        // Räkna poäng
        Map<String, Integer> scores = new HashMap<>();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                String color = grid[r][c];
                if (color != null) {
                    scores.put(color, scores.getOrDefault(color, 0) + 1);
                }
            }
        }



        // Hitta vinnare
        String winner = null;
        int maxPoints = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxPoints) {
                winner = entry.getKey();
                maxPoints = entry.getValue();
            }
        }

        System.out.println(">>> scores = " + scores);
        System.out.println(">>> winner = " + winner);

        // Skicka reslutat till klienten
        messagingService.broadcast("/topic/game/" + gameId, Map.of(
                "type", "roundEnd",
                "scores", scores,
                "winner", winner
        ));

    }

}
