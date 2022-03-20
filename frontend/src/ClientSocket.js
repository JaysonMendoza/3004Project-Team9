import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { setGameStarted } from "./Stores/GeneralStore";
import { useUpdatePlayArea } from "./Stores/PlayAreaStore";


let client;

const REGISTRATION_URL = "http://localhost:8080/api/register"
const SOCK_SERVER = "http://localhost:8080/quest-game-websocket"
const START_URL = "http://localhost:8080/api/start"

export async function connect(setConnected,setGameStarted, addNewMessage, setPlayers, name, updateHand, updatePlayer, setTurn, notifySponsorRequest, updateStageArea, updatePlayerPlayArea) {
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


    client.subscribe("/user/topic/player/hand-update", (message) => {
        let newHand = JSON.parse(message.body);
        console.log("Received hand update: " + newHand);
        updateHand(newHand);
    });


    client.subscribe("/topic/general/player-connect", (players) => {
      let body = JSON.parse(players.body);
      console.log("clientsocket players.body: " + players.body);
      const bodyKeys = Object.keys(body);
      console.log(bodyKeys);
      setPlayers(bodyKeys);
    });


    client.subscribe("/topic/general/game-start", () => {
      console.log("setting game started true");
      setGameStarted(true);
    });

    // client.subscribe("/topic/general/")

    client.subscribe("/topic/general/next-turn", (message) => {
      /**
       * Server informs about the next turn
       * This endpoind is used in all phases (general, quest, event, tournament)
       * and scenarios (next player to draw a story card, next player to answer the sponsor search request)
       * after the game has started
       */

      let body = JSON.parse(message.body)
      console.log("Received player-next-turn" + body.name);
      setTurn(body.name);
    });


    client.subscribe("/topic/player/hand-oversize" , (message) => {
      /**
       * Server informs that one player has oversized hand (more than 12 cards)
       * All activity in the game is blocked, this player must discard or play 
       * the card(s) until their hand has <= 12 cards to proceed
       */
      let body = JSON.parse(message.body);
      console.log("Player Hand Oversize: \n" + body + " \n\n ");
    });


    client.subscribe("/topic/player/player-update", (message) => {
      /**
       * Server informs about changes in player's state including their rank,
       * rankBattlePoint and the number of shield
       */
      let body = JSON.parse(message.body);
      updatePlayer(body);
    });


    client.subscribe("/topic/decks/deck-update", (message) => {
      /**
       * Server notifies clients of discard pile contents, and the number of cards
       * in a deck draw pile without revealing the cards.
       * This is to be used for visualization
       */

    });


    client.subscribe("/topic/play-areas/play-area-changed", (data) => {
      /**
       * This represents cards in play. It could be player play areas, or game stages
       */
      let body = JSON.parse(data.body);
      console.log("Play Area Update recieved: "+JSON.stringify(body));
      updatePlayerPlayArea(body);
    });

    client.subscribe("/topic/quest/stage-area-changed", (data) => {
      /**
       * This represents cards in play. It could be player play areas, or game stages
       */
      let body = JSON.parse(data.body);
      console.log("Stage Area Update recieved: " + JSON.stringify(body));
      updateStageArea(body);
    });


    client.subscribe("/topic/quest/sponsor-search", (message) => {
      /**
       * Server is querrying for sponsor
       * This happens right after a player drawn a quest card which started the quest
       * phase
       */
      console.log("Sponsor Search: " + message);
      let x = JSON.parse(message.body);
      notifySponsorRequest(x.name);
    });


    client.subscribe("/topic/quest/join-request", (message) => {
      /**
       * Server is querying for players to join quest
       * This happens after the quest has been setup and a quest stage has started
       */
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

export function drawCard(name, playerID) {
  console.log("Draw Story Card: \nName: " + name + "\nPlayerID: " + playerID);
  client.publish({
    destination: "/app/general/player-draw-card",
    body: JSON.stringify({
      name: name,
      playerID: playerID,
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

export function playCard(name, playerID, cardId, src, dst) {
  console.log("Play Card: \nName: " + name + "\nCardID: " + cardId + "\nPlayerID: " + JSON.stringify(playerID) + "\nsrc: " + src + "\ndst: " + dst + "\n");
  if (src === -1 && dst === -1) {
    client.publish({
      destination: "/app/general/player-play-card",
      body: JSON.stringify({
        name: name,
        playerID: playerID,
        cardId: cardId,
      }),
    });
  } else {
    client.publish({
      destination: "/app/quest/sponsor-play-card",
      body: JSON.stringify({
        name: name,
        playerID: playerID,
        cardId: cardId,
        src: src,
        dst: dst
      }),
    });
  }
}

export function sponsorRespond(name, sponsorDecision) {
  /**
   * Respond to a sponsor search request from the server
   */
  console.log(`Player name=${name} decides to ${sponsorDecision ? 'sponsor' : 'not sponsor'} the quest`);
  client.publish({
    destination: "/app/quest/sponsor-response",
    body: JSON.stringify({
      name: name,
      found: sponsorDecision
    })
  });
}

export function joinRespond(name, joinDecision) {
  /**
   * Respond to a quest stage join request from the server
   */
  console.log(`Player name=${name} decides to ${joinDecision ? 'join' : 'not join'} the the quest stage`);
  client.publish({
    destination: "/app/quest/join-response",
    body: JSON.stringify({
      name: name,
      joined: joinDecision
    })
  });
}


export async function setupComplete(name, playerID) {
  /**
   * Respond to a quest stage join request from the server
   */
  console.log(`name=${name} playerID=${playerID}`);
  let response = await fetch("http://localhost:8080/api/quest/setup-complete", {
    method: "POST",
    body: JSON.stringify({
      name: name,
      playerID: playerID
    }),
    headers: {
      "Content-Type": "application/json"
    }
  })

  let body = await response.json();
  console.log(`Player name=${body.name} has finished setting up: ${body.confirmed}`);
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
    //setGameStarted(true);
}

