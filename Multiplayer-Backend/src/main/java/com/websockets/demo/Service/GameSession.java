package com.websockets.demo.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameSession {

    public static final int MAX_PLAYERS = 4;
    public static final int GRID_SIZE = 15;

    private final String sessionId;
    private final String[][] grid;
    private final Map<String, String> players;
    private final Map<String, Integer> scores;
    private String phase;
    private long roundEndsAt;

    private final List<String> colors = Arrays.asList("red", "green", "yellow", "blue");

    // Konstruktor för att skapa en ny session
    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.grid = new String[GRID_SIZE][GRID_SIZE];
        this.players = new LinkedHashMap<>();
        this.scores = new HashMap<>();
        this.phase = "lobby";
    }

    public String getSessionId() {
        return sessionId;
    }

    // Lägger till en spelare i sessionen
    public synchronized String assignPlayer(String playerSessionId) {
        if (players.containsKey(playerSessionId))
            return players.get(playerSessionId);
        if (players.size() >= MAX_PLAYERS)
            return null;

        String color = colors.get(players.size());
        players.put(playerSessionId, color);
        scores.put(color, 0);
        return color;
    }

    public synchronized boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }

    public synchronized Map<String, String> getPlayers() {
        return players;
    }

    public synchronized Map<String, Integer> getScores() {
        return scores;
    }

    public synchronized String[][] getGrid() {
        return grid;
    }

    public synchronized void resetGrid() {
        for (int r = 0; r < GRID_SIZE; r++) {
            Arrays.fill(grid[r], null);
        }
        scores.replaceAll((k, v) -> 0);
    }

    // Uppdaterar en cell på spelplanen
    public synchronized void updateCell(int row, int col, String color) {
        String oldColor = grid[row][col];
        if (oldColor != null)
            scores.put(oldColor, scores.get(oldColor) - 1);
        grid[row][col] = color;
        if (color != null)
            scores.put(color, scores.get(color) + 1);
    }

    // Startar en ny runda
    public synchronized void startRound(long roundDurationMs) {
        resetGrid();
        phase = "running";
        roundEndsAt = System.currentTimeMillis() + roundDurationMs;
    }

    public synchronized void endRound() {
        phase = "ended";
    }

    // Hämtar fasen för spelarens session
    public String getPhase() {
        return phase;
    }

    public long getRoundEndsAt() {
        return roundEndsAt;
    }
}
