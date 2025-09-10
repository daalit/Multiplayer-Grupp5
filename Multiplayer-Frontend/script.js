const socket = new SockJS("http://localhost:8080/websocket");
const stompClient = Stomp.over(socket);

const gridElement = document.getElementById("grid");
const scoreList = document.getElementById("scoreList");

let playerColor = null;

const gridState = Array.from({ length: 15 }, () => Array(15).fill(null));
let scores = {};

let gameRunning = false; // för att veta om spelet är startat eller inte -


const TOTAL_CELLS = 15 * 15;

function renderGrid() {
  gridElement.innerHTML = "";

  for (let row = 0; row < 15; row++) {
    for (let col = 0; col < 15; col++) {
      let cell = document.createElement("div");
      cell.classList.add("cell");
      if (gridState[row][col]) {
        cell.style.background = gridState[row][col];
      }
      cell.addEventListener("click", () => {

          if (!gameRunning) { // går ej starta om gameRunning inte är true 
              alert("Du kan inte klicka förrän spelet är startat!");
              return;
          }
        if (playerColor) {
          stompClient.send("/app/grid", {}, JSON.stringify({ row, col, color: playerColor }));
          stompClient.send("/app/scores", {}); // be om uppdatering
        } else {
          alert("Du har inte blivit tilldelad en färg ännu!");
        }
      });
      gridElement.appendChild(cell);
    }
  }
}

function renderScores() {
  scoreList.innerHTML = "";
  Object.entries(scores).forEach(([color, points]) => {
    const percent = Math.min((points / TOTAL_CELLS) * 100, 100);

    const entry = document.createElement("div");
    entry.classList.add("score-entry");

    const label = document.createElement("div");
    label.classList.add("score-label");
    label.innerText = `${color.toUpperCase()} (${points})`;

    const bar = document.createElement("div");
    bar.classList.add("progress-bar");

    const fill = document.createElement("div");
    fill.classList.add("progress-fill");
    fill.style.background = color;
    fill.style.width = percent + "%";

    bar.appendChild(fill);
    entry.appendChild(label);
    entry.appendChild(bar);

    scoreList.appendChild(entry);
  });
}

stompClient.connect({}, (frame) => {
  console.log("Connected:", frame);


    //koppla start-knappen
    document.getElementById("startBtn").addEventListener("click", () => {
    let countdown = 5;
    const statusEl = document.getElementById("status");
    statusEl.innerText = `Spelet startar om ${countdown}...`;

    const interval = setInterval(() => {
        countdown--;
        if (countdown > 0) {
            statusEl.innerText = `Spelet startar om ${countdown}...`;
        } else {
            clearInterval(interval);
            statusEl.innerText = "Spelet startar!";
            stompClient.send("/app/start", {}, ""); // triggar GameController startgame()
        }
    }, 1000);
});

  // Lyssna på grid updates
  stompClient.subscribe("/topic/grid", (message) => {
    const data = JSON.parse(message.body);
    gridState[data.row][data.col] = data.color;
    renderGrid();
  });



  // Lyssna på start/slut
    stompClient.subscribe("/topic/game", (message) => {
        const data = JSON.parse(message.body);

        if (data.type === "roundStart") {
            gameRunning = true; // Spelet körs
            for(r = 0; r < 15; r++) {
                for (c = 0; c < 15; c++) {
                    gridState[r][c] = null;
                }
            }
            renderGrid();

            document.getElementById("status").innerText =
                "Spelet startat! ";
                document.getElementById("startBtn").textContent = "Restart";
        }


        if (data.type === "roundEnd") {
            gameRunning = false; // spelet har avslutats
            document.getElementById("status").innerText =
                "Rundan slut!  Vinnare";
                document.getElementById("startBtn").textContent = "Starta spel";
        }
    });

  // Lyssna på player-assignments
  stompClient.subscribe("/topic/players", (message) => {
    const data = JSON.parse(message.body);
    if (!playerColor && data.sessionId === stompClient.ws._transport.url) {
      playerColor = data.color;
      alert("Du fick färgen: " + playerColor.toUpperCase());
    }
  });

  // Lyssna på poäng
  stompClient.subscribe("/topic/scores", (message) => {
    scores = JSON.parse(message.body).scores;
    renderScores();
  });

  // Skicka join request
  stompClient.send("/app/join", {}, stompClient.ws._transport.url);
  stompClient.send("/app/scores", {}); // hämta första scorelistan

  renderGrid();
});
