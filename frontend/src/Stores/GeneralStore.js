import create from "zustand";

const useStore = create((set) => ({
  title: "Quest Game",
  connected: false,
  gameStarted: false,
  name: "",
  messages: [],
  players: [],
  hands: [],
  events: "",
  loadPlayers: async () => {
    await handleLoadPlayers(set);
  },
  setConnected: (connected) => set(() => ({ connected: connected })),
  setName: (name) => set(() => ({ name: name })),
  setGameStarted: (gameStarted) => set(() => ({ gameStarted: gameStarted })),
  setPlayers: (players) => set(() => ({ players: players })),
  updatePlayer: (player) =>
  set((current) => ({
    players: (() => {
      let playerExist = false;
      for (let i = 0; i < current.players.length; i++) {
        if (current.players[i].playerName === player.playerName) {
          playerExist = true;
        }
      }
      if (playerExist) {
        return current.players.map((currPlayer) => {
          if (currPlayer.playerName === player.playerName) {
            return player;
          } else {
            return currPlayer;
          }
        });
      } else {
        return [...current.players, player];
      }
    })
  })),
  updateHand: (hand) =>
    set((current) => ({
      hands: (() => {
        //console.log("hand = " + JSON.stringify(hand));
        let playerExist = false;
        for (let i in current.hands) {
          if (current.hands[i].playerName === hand.playerName) {
            playerExist = true;
          }
        }
        //console.log("playerExist = " + playerExist);
        if (playerExist) {
          return current.hands.map((currHand) => {
            if (currHand.playerName === hand.playerName) {
              return hand;
            } else {
              return currHand;
            }
          });
        } else {
          return [...current.hands, hand];
        }
      })(),
    })),
  addNewMessage: (name, message) =>
    set((current) => ({
      messages: [...current.messages, { name: name, message: message }],
    })),
}));

const handleLoadPlayers = async (setPlayers) => {
  fetch("http://localhost:8080/api/player")
    .then((response) => response.json())
    .then((players) => {
      setPlayers({ players: players });
    })
    .catch((error) => {
      console.log("eror in handleLoadPlayers: " + error);
    });
};

export const useTitle = () => useStore((state) => state.title);
export const useConnected = () => useStore((state) => state.connected);
export const useName = () => useStore((state) => state.name);
export const useMessages = () => useStore((state) => state.messages);
export const useGameStarted = () => useStore((state) => state.gameStarted);
export const usePlayerHands = () => useStore((state) => state.hands);

export const usePlayers = () => useStore((state) => state.players);
export const useAddNewPlayer = () => useStore((state) => state.addNewPlayer);

export const useSetConnected = () => useStore((state) => state.setConnected);
export const useSetGameStarted = () =>
  useStore((state) => state.setGameStarted);
export const useSetName = () => useStore((state) => state.setName);
export const useAddNewMessage = () => useStore((state) => state.addNewMessage);

export const useUpdateHand = () => useStore((state) => state.updateHand);

export const useUpdatePlayer = () => useStore((state) => state.updatePlayer);

export const useSetPlayers = () => useStore((state) => state.setPlayers);

export const useLoadPlayers = () => useStore((state) => state.loadPlayers);

export default useStore;
