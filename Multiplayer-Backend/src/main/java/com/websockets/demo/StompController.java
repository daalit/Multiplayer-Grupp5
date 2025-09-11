package com.websockets.demo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.websockets.demo.Service.GameManager;
import com.websockets.demo.Service.GameSession;
import com.websockets.demo.Service.MessagingService;

@Controller
public class StompController {

    @Autowired
    private GameManager gameManager;

    @Autowired
    private MessagingService messagingService;

    // En spelare går med i spel och får en färg och ett sessionId

    @MessageMapping("/join")
    @SendToUser("/topic/players")
    public PlayerAssignment join(@Header("simpSessionId") String simpSessionId) {
        // Lägger till spelaren i en session
        GameSession session = gameManager.assignPlayerToSession(simpSessionId);

        // Hämtar spelarens färg
        String color = session.getPlayers().get(simpSessionId);

        // Skickar ut antalet spelare i sessionen
        messagingService.broadcast(
                "/topic/session/" + session.getSessionId() + "/players",
                Map.of("playerCount", session.getPlayers().size()));

        // Skickar tillbaka spelarens färg och sessionId
        return new PlayerAssignment(session.getSessionId(), color);
    }

    // Uppdaterar cellerna på spelplanen
    @MessageMapping("/grid/{gameId}")
    public void handleGrid(@DestinationVariable String gameId, GridMessage message) {
        GameSession session = gameManager.getSession(gameId);
        if (session != null) {
            session.updateCell(message.getRow(), message.getCol(), message.getColor());
            messagingService.broadcast("/topic/grid/" + gameId, message);
            messagingService.broadcast("/topic/scores/" + gameId, new ScoreUpdate(session.getScores()));
        }
    }

    // Startar en ny runda

    @MessageMapping("/start/{gameId}")
    public void startGame(@DestinationVariable String gameId) {
        GameSession session = gameManager.getSession(gameId);
        if (session != null && !"running".equals(session.getPhase())) {
            session.startRound(30000); // 30 seconds
            messagingService.broadcast("/topic/game/" + gameId, Map.of(
                    "type", "roundStart",
                    "roundEndsAt", session.getRoundEndsAt()));

            new Thread(() -> {
                try {
                    Thread.sleep(30000);
                    session.endRound();
                    messagingService.broadcast("/topic/game/" + gameId, Map.of(
                            "type", "roundEnd"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // Hämtar poängen för en session
    @MessageMapping("/scores/{gameId}")
    public void getScores(@DestinationVariable String gameId) {
        GameSession session = gameManager.getSession(gameId);
        if (session != null) {
            messagingService.broadcast("/topic/scores/" + gameId, new ScoreUpdate(session.getScores()));
        }
    }
}
