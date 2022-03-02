import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import useStore, { useAddNewPlayer, useAddNewMessage } from "./Store";

let client;

const REGISTRATION_URL = "http://localhost:8080/api/register"
const SOCK_SERVER = "http://localhost:8080/quest-game-websocket"
const PLAYERS_URL = "http://localhost:8080/api/player"


export async function connect(setConnected, addNewMessage, name) {
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

export function disconnect() {
  client.disconnect();
}

export function startGame(){
  fetch('http://localhost:8080/api/start',
  {method: "POST",
  headers: {
      "Content-Type": "application/json"
    }} );
}

