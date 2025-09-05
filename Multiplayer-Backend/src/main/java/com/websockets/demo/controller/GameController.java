package com.websockets.demo.controller;

import com.websockets.demo.Service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;


    //Fångar upp när frontend skickar till /app/start

    @MessageMapping("/start")
    public void startGame (){
        gameService.startGame();
    }
}
