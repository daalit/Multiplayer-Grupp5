const socket = new SockJS("http://localhost:8080/websocket");
const stompClient = Stomp.over(socket);

const gridElement = document.getElementById("grid");
const scoreList = document.getElementById("scoreList");

let timerInterval = null;

let playerColor = null;
let gameId = null; // sessionId
let gameRunning = false;

const gridState = Array.from({ length: 15 }, () => Array(15).fill(null));
let scores = {};

const TOTAL_CELLS = 15 * 15;

// Renderar spelplanen
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
        if (!gameRunning) {
          alert("You can’t click until the game has started!");
          return;
        }
        if (playerColor && gameId) {
          stompClient.send(
            `/app/grid/${gameId}`,
            {},
            JSON.stringify({ row, col, color: playerColor })
          );
          stompClient.send(`/app/scores/${gameId}`, {});
        } else {
          alert("You don’t have a color yet!");
        }
      });
      gridElement.appendChild(cell);
    }
  }
}

function startTimer(endTime) {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
    
  timerInterval = setInterval(() => {
    const now = Date.now();
    const timeLeft = Math.max(0, endTime - now);
    
    if (timeLeft <= 0) {
      timer.innerHTML = "<h2>Tid: 0s</h2>";
      clearInterval(timerInterval);
      return;
    }
    
    const seconds = Math.ceil(timeLeft / 1000);
    timer.innerHTML = `<h2>Tid: ${seconds}s</h2>`;
  }, 100);
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
  timer.innerHTML = "<h2>Tid: Round ended!</h2>";
}

// Renderar poängen
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

  // Knappen för att gå med i spel
  document.getElementById("joinBtn").addEventListener("click", () => {
    stompClient.send("/app/join", {}, {});
  });

  // Hanterar spelares tilldelning (session och färg)
  stompClient.subscribe("/user/topic/players", (message) => {
    const assignment = JSON.parse(message.body);
    gameId = assignment.sessionId;
    playerColor = assignment.color;

    alert("You were assigned color: " + playerColor.toUpperCase());
    document.getElementById("joinBtn").style.display = "none";

    // Lyssnar på sessionens uppdateringar
    stompClient.subscribe(`/topic/session/${gameId}/players`, (msg) => {
      const payload = JSON.parse(msg.body);
      if (payload.playerCount >= 2) {
        document.getElementById("startBtn").style.display = "block";
      }
    });

    // Lyssnar på spelplanens uppdateringar
    stompClient.subscribe(`/topic/grid/${gameId}`, (msg) => {
      const data = JSON.parse(msg.body);
      gridState[data.row][data.col] = data.color;
      renderGrid();
    });

    // Lyssnar på poängens uppdateringar
    stompClient.subscribe(`/topic/scores/${gameId}`, (msg) => {
      scores = JSON.parse(msg.body).scores;
      renderScores();
    });

    // Lyssnar på spelens uppdateringar
    stompClient.subscribe(`/topic/game/${gameId}`, (msg) => {
      const data = JSON.parse(msg.body);

      if (data.type === "roundStart") {
        gameRunning = true;
        for (let r = 0; r < 15; r++) {
          for (let c = 0; c < 15; c++) {
            gridState[r][c] = null;
          }
        }
        renderGrid();
        document.getElementById("status").innerText = "Game started!";

        startTimer(data.roundEndsAt);
      }

      if (data.type === "roundEnd") {
        gameRunning = false;
        document.getElementById("status").innerText = "Round ended!";
        stopTimer();
      }
    });

    // Hämtar poängen
    stompClient.send(`/app/scores/${gameId}`, {});
  });

  // Knappen för att starta spel
  document.getElementById("startBtn").addEventListener("click", () => {
    if (gameId) {
      stompClient.send(`/app/start/${gameId}`, {});
    }
  });

  renderGrid();
});
