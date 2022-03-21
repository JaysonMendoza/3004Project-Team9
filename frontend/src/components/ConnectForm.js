import React, { useState } from "react";
import { connect } from "../ClientSocket";

import {
  useName,
  useSetConnected,
  useSetName,
  useAddNewMessage,
  useAddNewPlayer,
  useSetPlayers,
  useSetGameStarted,
  useUpdateHand,
  useUpdatePlayer,
  useSetTurn,
  useSetSponsorRequest,
  useSetIsSponsoring,
  useSetJoinRequest,
  useSetHandOversize,
  useSetActivePlayers,
  useSetFoeStageStart,
  useSetStoryCard,
  useSetNotifyStageStart,
  useSetNotifyStageEnd,
  useSetNotifyQuestEnd,
  useSetNotifyHandOversize,
  useSetNotifyHandNotOversize
} from "../Stores/GeneralStore";
import {
  useUpdateStageArea,
  useUpdatePlayerPlayArea
} from "../Stores/PlayAreaStore"

const ConnectForm = () => {
  const name = useName();
  const addNewMessage = useAddNewMessage();
  const addNewPlayer =  useAddNewPlayer();
  const setPlayers = useSetPlayers();
  const setConnected = useSetConnected();
  const setGameStarted = useSetGameStarted();
  const updateHand = useUpdateHand();
  const updateStageArea = useUpdateStageArea();
  const updatePlayer = useUpdatePlayer();
  const setTurn = useSetTurn();
  const notifySponsorRequest = useSetSponsorRequest();
  const updatePlayerPlayArea = useUpdatePlayerPlayArea();
  const setJoinRequest = useSetJoinRequest();
  const setHandOversize = useSetHandOversize();
  const setActivePlayers = useSetActivePlayers();
  const setFoeStageStart = useSetFoeStageStart();
  const setStoryCard = useSetStoryCard();
  const setNotifyStageStart = useSetNotifyStageStart();
  const setNotifyStageEnd = useSetNotifyStageEnd();
  const setNotifyQuestEnd = useSetNotifyQuestEnd();
  const setNotifyHandOversize = useSetNotifyHandOversize();
  const setNotifyHandNotOversize = useSetNotifyHandNotOversize();

  const connectFunctions = {
    name,
    setConnected,
    setGameStarted,
    addNewMessage,
    setPlayers,
    updateHand,
    updatePlayer,
    setTurn,
    notifySponsorRequest,
    updateStageArea,
    updatePlayerPlayArea,
    setJoinRequest,
    setHandOversize,
    setActivePlayers,
    setFoeStageStart,
    setStoryCard,
    setNotifyStageStart,
    setNotifyStageEnd,
    setNotifyQuestEnd,
    setNotifyHandOversize,
    setNotifyHandNotOversize,
  }

  const setName = useSetName();
  // Preprocess before making connection request
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name) {
      alert("Please enter your name");
      return;
    }
    if (connect(connectFunctions) === false) {
      alert("Name already taken, please choose a different one")
    }
  };

  return (
    <form className="m-3" onSubmit={handleSubmit}>
      <div className="input-group mb-2 mt-4">
        <input
          type="text"
          className="form-control"
          placeholder="Enter your name"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
          }}
        />
      </div>
      <button type="submit" className="btn btn-primary">
        Connect
      </button>
    </form>
  );
};

export default ConnectForm;
