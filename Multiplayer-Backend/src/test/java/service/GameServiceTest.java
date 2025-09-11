package service;

import com.websockets.demo.Service.GameService;
import com.websockets.demo.Service.MessagingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class GameServiceTest {

    private GameService gameService;
    private MessagingService messagingServiceMock;

    @BeforeEach
    void setUp() {
        messagingServiceMock = Mockito.mock(MessagingService.class);
        gameService = new GameService();
        gameService.messagingService = messagingServiceMock; // injicera mock
        gameService.setTestMode(true); // gör så att vi slipper 30s väntan i tester
    }


    //test 1: Starta spelet
    @Test
    void shouldStartGame() {
        gameService.startGame();

        //kolla att phase ändras
        assertEquals("running", gameService.getPhase());

        //roundEndsAt ska vara satt
        assertNotNull(gameService.getRoundEndsAt());

        // verifiera att boradvast anropades
        verify(messagingServiceMock, atLeastOnce()).broadcast(any());
    }

    // test 2 spelet ska ej kunna startas på nytt när de redan är startat
    @Test
    void shouldNotBeAbleToStartGameIfAlreadyRunning(){
        gameService.startGame();
        Long firstRoundEndsAt = gameService.getRoundEndsAt();

        //Försök starta igen
        gameService.startGame();

        // samma timestamp == nytt spel startade inte
        assertEquals(firstRoundEndsAt, gameService.getRoundEndsAt());
    }


    // TODO: Max antal spelare
    // TODO: Min antal spelare
}
