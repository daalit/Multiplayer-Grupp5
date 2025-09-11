package com.websockets.demo.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class GameManager {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger();

    public synchronized GameSession assignPlayerToSession(String playerSessionId) {
        // Hittar en session som inte är full
        for (GameSession session : sessions.values()) {
            if (!session.isFull()) {
                session.assignPlayer(playerSessionId);
                return session;
            }
        }
        // Ingen ledig plats i session så vi skapar en ny
        String newId = "game-" + idCounter.incrementAndGet();
        GameSession newSession = new GameSession(newId);
        newSession.assignPlayer(playerSessionId);
        sessions.put(newId, newSession);
        return newSession;
    }

    public GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public Collection<GameSession> getAllSessions() {
        return sessions.values();
    }
}
