# Multiplayer-Grupp5
Multiplayer grupp 5 - Färgspel

**Java 17**

**Maven**

**Dependencies**

- Websocket

- Spring boot dev tools

- Spring Web




**GAME URL :** https://seal-app-gg7f8.ondigitalocean.app/ 


**Game Overview**

This project is a multiplayer color game for 2-4 players where the objective is to claim the most cells on a 15x15 grid by the end of a round (30 seconds). When a player joins, they're assigned a color. During a round, players click on the grid to change a cell's color to their own, earning points. The game tracks scores and declares a winner at the end of each round.

**Features**

    Real-time Gameplay: The game uses WebSockets to update the grid and player scores in real time for all connected players.

    Dynamic Grid: Players can click on any cell to change its color, visually representing who controls which part of the board.

    Scoring System: Points are awarded for each cell a player claims, and a progress bar displays the percentage of the grid each player controls.

    Player Status: The game tracks the number of connected players and only allows the game to be started once at least two players have joined.

    Round Timer: Each game round is timed.

    Winner Display: The winner of each round is announced along with their final score.

**Local Installation and Play Guide**

    1. Clone the Repository: Clone the project to your local machine.

    2. Build and Run the Application: Ensure you have Java 17 installed. The project uses Spring Boot, so you can build and run the application using Maven. The dependencies required are websocket, spring boot dev tools, and Spring Web.

    3. Start a Live Server: Use a live server extension or a similar tool to host the index.html file.

    4. Open in Browser: Navigate to http://127.0.0.1:5500/ in your web browser.

    5. Join the Game: Click the "Gå med i spel" (Join game) button to get a player color assigned.

    6. Start the Game: At least two players need to join before the "Starta spel" (Start game) button appears. Once it does, any player (except the last to join) can click it to begin the round.

    7. Play: Click on the grid cells to claim them for your color.

    8. The player with the most claimed cell's wins. 
