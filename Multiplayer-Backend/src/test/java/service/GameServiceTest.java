package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.websockets.demo.Service.GameManager;
import com.websockets.demo.Service.GameService;
import com.websockets.demo.Service.GameSession;
import com.websockets.demo.Service.MessagingService;

public class GameServiceTest {

    private GameService gameService;
    private GameSession gameSession;
    private GameManager gameManagerMock;
    private MessagingService messagingServiceMock;
    private String testGameId = "test-game-1";
   

    @BeforeEach
    void setUp() {
        messagingServiceMock = Mockito.mock(MessagingService.class);
        gameManagerMock = Mockito.mock(GameManager.class);
        gameSession = new GameSession(testGameId);     
    
        gameService = new GameService();
        gameService.messagingService = messagingServiceMock;

        when(gameManagerMock.getSession(testGameId)).thenReturn(gameSession);    
    }


    //test 1: Starta spelet
    @Test
    void shouldStartGame() {
        
        when(gameManagerMock.getSession(testGameId)).thenReturn(gameSession);

        //Injectar mockdatan som skapas setUp till GameService. 
        try {
            java.lang.reflect.Field gameManagerField = GameService.class.getDeclaredField("gameManager");
            gameManagerField.setAccessible(true);
            gameManagerField.set(gameService, gameManagerMock);
        }catch(Exception e){
            System.out.println("Error vid start av spel (TEST)");
        }

        gameService.startGame(testGameId);       

        //kolla att phase ändras
        assertEquals("running", gameSession.getPhase());

        //Kollar så rätt sessionId används (test id)
        assertEquals("test-game-1", gameSession.getSessionId());        

        //roundEndsAt ska vara satt
        assertNotNull(gameSession.getRoundEndsAt());

        // verifiera att boradvast anropades
        verify(messagingServiceMock, atLeastOnce()).broadcast(anyString(), any());
    }

    // test 2 spelet ska ej kunna startas på nytt när de redan är startat
   /*  @Test
    void shouldNotBeAbleToStartGameIfAlreadyRunning(){
        gameService.startGame();
        Long firstRoundEndsAt = gameService.getRoundEndsAt();

        //Försök starta igen
        gameService.startGame();

        // samma timestamp == nytt spel startade inte
        assertEquals(firstRoundEndsAt, gameService.getRoundEndsAt());
    }
 */

    // TODO: Max antal spelare
    // TODO: Min antal spelare
}
