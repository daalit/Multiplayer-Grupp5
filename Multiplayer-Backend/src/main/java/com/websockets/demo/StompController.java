package com.websockets.demo;

import java.util.Map;

import com.websockets.demo.Service.GameService;
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
    @Autowired
    private GameService gameService;

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
        gameService.startGame(gameId);
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
