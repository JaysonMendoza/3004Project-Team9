import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";


let client;

const REGISTRATION_URL = "http://localhost:8080/api/register"
const SOCK_SERVER = "http://localhost:8080/quest-game-websocket"
const START_URL = "http://localhost:8080/api/start"


export async function connect(setConnected, addNewMessage, setPlayers, name) {
  console.log("Attempt connection");

  // Register player name
  let response = await fetch(REGISTRATION_URL, {
    method: "POST",
    body: JSON.stringify({
      name: name
    }),
    headers: {
      "Content-Type": "application/json"
    }
  })

  let body = await response.json();
  if (!body.confirmed || body.name !== name) {
    console.log("Connection declined or unmatch name");
    return false;
  }

  // Perform handshake with the registered name
  client = new Client({
    webSocketFactory: () => {
      return new SockJS(`${SOCK_SERVER}?name=${name}`);
    },
    reconnectDelay: 50000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });


  client.onConnect = (frame) => {
    console.log("Connection successful");
    setConnected(true);
    client.subscribe("/topic/message", (message) => {
      let body = JSON.parse(message.body);
      console.log(body);
      addNewMessage(body.name, body.message);
    });
    client.subscribe("/topic/general/next-turn", (name) => {
        console.log("Turn is now: " + name.body);
//        setTurn(name);
    });
    client.subscribe("/topic/player/hand-update", (message) => {
        let newHand = JSON.parse(message.body);
        console.log("New Hand: " + message.body);
//        updateHand(newHand.name,newHand.hand);
    });
    client.subscribe("/topic/general/player-connect", (players) => {
      let body = JSON.parse(players.body);
      console.log("clientsocket players.body: " + players.body);
      const bodyKeys = Object.keys(body);
      console.log(bodyKeys);
      setPlayers(bodyKeys);
    })
  };

  client.onDisconnect = () => {
    disconnect();
    setConnected(false);
  };

  client.onStompError = function (frame) {
    console.log("Broker reported error: " + frame.headers["message"]);
    console.log("Additional details: " + frame.body);
  };

  client.activate();
  return true;
}

export function sendMessage(name, message) {
  client.publish({
    destination: "/app/message",
    body: JSON.stringify({
      name: name,
      message: message,
    }),
  });
}

export function drawCard(name, cardId) {
  console.log("Draw Card: \nName: " + name + "\nCardID: " + cardId);
  client.publish({
    destination: "/app/general/player-draw-card",
    body: JSON.stringify({
      name: name,
      cardId: cardId, //server will not care about this
    }),
  });
}

export function discardCard(name, cardId) {
  console.log("Discard Card: \nName: " + name + "\nCardID: " + cardId);
  client.publish({
    destination: "/app/general/player-discard-card",
    body: JSON.stringify({
      name: name,
      cardId: cardId,
    }),
  });
}

export function disconnect() {
  client.disconnect();
}

export function startGame(){
  fetch(START_URL,
  {method: "POST",
  headers: {
      "Content-Type": "application/json"
    }} );
}

