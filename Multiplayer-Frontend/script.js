const socket = new SockJS("http://localhost:8080/websocket");
const stompClient = Stomp.over(socket);

const gridElement = document.getElementById("grid");
let playerColor = null;

const gridState = Array.from({ length: 15 }, () => Array(15).fill("null"));

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
        if (playerColor) {
          stompClient.send("/app/grid", {}, JSON.stringify({ row, col, color: playerColor }));
        } else {
          alert("Du har inte blivit tilldelad en färg ännu!");
        }
      });
      gridElement.appendChild(cell);
    }
  }
}

stompClient.connect({}, (frame) => {
  console.log("Connected:", frame);

  // Lyssna på grid updates
  stompClient.subscribe("/topic/grid", (message) => {
    const data = JSON.parse(message.body);
    gridState[data.row][data.col] = data.color;
    renderGrid();
  });

  // Lyssna på player-assignments
  stompClient.subscribe("/topic/players", (message) => {
    const data = JSON.parse(message.body);
    if (!playerColor && data.sessionId === stompClient.ws._transport.url) {
      playerColor = data.color;
      alert("Du fick färgen: " + playerColor);
    }
  });

  // Skicka join request
  stompClient.send("/app/join", {}, stompClient.ws._transport.url);

  renderGrid();
});
