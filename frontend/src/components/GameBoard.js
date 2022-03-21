import PlayerHand from "./PlayerHand";
import QuestDisplay from "./QuestDisplay";
import CardImages from "../Images/index";
import Popup from "./Popup";
import Card from "./Card";
import { drawCard, sponsorRespond, setupComplete, participantSetupComplete } from "../ClientSocket";
import { useName, usePlayerHands, usePlayers, useTurn, useSponsorRequest, useActivePlayers, useSetPopupType, usePopupType, useIsSponsoring, useSetIsSponsoring, useJoinRequest, useSetJoinRequest, useFoeStageStart, useStoryCard, useHandOversize, useSetFoeStageStart, useNotifyStageStart, useNotifyStageEnd, useNotifyQuestEnd, useSetNotifyStageStart, useSetNotifyStageEnd, useSetNotifyQuestEnd, useNotifyHandOversize, useSetNotifyHandOversize, useNotifyHandNotOversize, useSetNotifyHandNotOversize} from "../Stores/GeneralStore";
import { useUpdatePlayArea, usePlayerPlayAreas, useStageAreas } from "../Stores/PlayAreaStore";
import { Button } from "react-bootstrap";
import React, { useState, useEffect } from "react";
import "./GameBoard.css";

function GameBoard(props) {
    let init = 80;
    let jump = 240;
    const name = useName();
    const popupType = usePopupType();
    const allPlayers = usePlayers();
    let hands = usePlayerHands();
    let active = usePlayerPlayAreas();
    let stageAreas = useStageAreas();
    const turn = useTurn();
    let sponsorRequest = useSponsorRequest();
    // const setPopupType = useSetPopupType();
    const isSponsoring = useIsSponsoring();
    const setIsSponsoring = useSetIsSponsoring();
    const joinRequest = useJoinRequest();
    const activePlayers = useActivePlayers();
    console.log("Active Players are: " + JSON.stringify(activePlayers));
    const [popup, setPopup] = useState(true);
    const [foeStageStartPopup, setFoeStageStartPopup] = useState(true);
    // const [popup, setPopup] = useState(true);
    const [foeStageStart, setFoeStageStart] = [useFoeStageStart(), useSetFoeStageStart()];
    const storyCard = useStoryCard();
    // const handOversize = useHandOversize();
    const [notifyStageStart, setNotifyStageStart] = [useNotifyStageStart(), useSetNotifyStageStart()];
    const [notifyStageEnd, setNotifyStageEnd] = [useNotifyStageEnd(), useSetNotifyStageEnd()];
    const [notifyQuestEnd, setNotifyQuestEnd] = [useNotifyQuestEnd(), useSetNotifyQuestEnd()];
    const [notifyHandOversize, setNotifyHandOversize] = [useNotifyHandOversize(), useSetNotifyHandOversize()];
    const [notifyHandNotOverSize, setNotifyHandNotOversize] = [useNotifyHandNotOversize(), useSetNotifyHandNotOversize()];

    // useEffect(() => {
        // if (handOversize) {
        //     setPopup(true);
        // }
        // if (foeStageStart) {
        //     setFoeStageStartPopup(true);
        // }
    // }, [handOversize, foeStageStart])

    // const togglePopup = () => {
    //     setPopup(!popup);
    //     console.log("trigger popup: " + popup);
    // }

    // useEffect(() => {
    //     setPopupType("SPONSORQUEST")
    // }, [])

    // useEffect(() => {
    //     if (joinRequest) {
    //         setPopupType("JOINQUEST")
    //     }
    // }, [joinRequest])


    // hands = [{name:"Test1",isTurn:true,hand:[{cardId:1,cardImage:CardImages.Ally_KingArthur}],cardsInPlay:[],rank:CardImages.Rank_Squire,shields:54},
    // {name:"Test2",isTurn:true,hand:[{cardId:2,cardImage:CardImages.Ally_KingArthur}],cardsInPlay:[],rank:CardImages.Rank_Squire,shields:5}];

    let myHandArr = [false, false, false, false];
    let myPlayerID = -1;
    for (let i = 0; i < hands.length; i++) {
        if (hands[i].playerName === name) {
            myHandArr[i] = true;
            myPlayerID = hands[i].playerId;
        }
    }

    const findPlayAreaById = (id) => {
        const finder = (playArea) => {
            if (playArea.id === id) {
                return true;
            }
            return false;
        }
        return finder
    }

    const getActiveCard = (playerId) => {
        return active.find(findPlayAreaById(playerId)) != undefined
            ? active.find(findPlayAreaById(playerId)).cardsInPlay : []
    }

    return (
        <div id="GameBoard">
            { storyCard && <img src={storyCard.imgSrc} alt="Story Card"/> }

            <div id="allHands">
                <PlayerHand
                    playerName={hands[0].playerName}
                    playerID={hands[0].playerId}
                    isTurn={(hands[0].playerName === turn)}
                    isMyHand={myHandArr[0]}
                    cardsInHand={hands[0].hand}
                    activeCards={getActiveCard(hands[0].playerId)}
                    rank={CardImages.Rank_Squire/*hands[0].rank*/}
                    numShields={5/*hands[0].shields*/}
                    top={init}
                    left={0}
                    shield={CardImages.Shield_3}
                    numStages={stageAreas.length}>
                </PlayerHand>
                <PlayerHand
                    playerName={hands[1].playerName}
                    playerID={hands[1].playerId}
                    isTurn={(hands[1].playerName === turn)}
                    isMyHand={myHandArr[1]}
                    cardsInHand={hands[1].hand}
                    activeCards={getActiveCard(hands[1].playerId)}
                    rank={CardImages.Rank_Squire/*hands[1].rank*/}
                    numShields={5/*hands[1].shields*/}
                    top={init + jump}
                    left={0}
                    shield={CardImages.Shield_1}
                    numStages={stageAreas.length}>
                </PlayerHand>
                {hands.length > 2 &&
                    <PlayerHand
                        playerName={hands[2].playerName}
                        playerID={hands[2].playerId}
                        isTurn={(hands[2].playerName === turn)}
                        isMyHand={myHandArr[2]}
                        cardsInHand={hands[2].hand}
                        activeCards={getActiveCard(hands[2].playerId)}
                        rank={CardImages.Rank_Squire/*hands[2].rank*/}
                        numShields={5/*hands[2].shields*/}
                        top={init + jump * 2}
                        left={0}
                        numStages={stageAreas.length}
                        shield={CardImages.Shield_8}>
                    </PlayerHand>
                }
                {hands.length > 3 &&
                    <PlayerHand
                        playerName={hands[3].playerName}
                        playerID={hands[3].playerId}
                        isTurn={(hands[3].playerName === turn)}
                        isMyHand={myHandArr[3]}
                        cardsInHand={hands[3].hand}
                        activeCards={getActiveCard(hands[3].playerId)}
                        rank={CardImages.Rank_Squire/*hands[3].rank*/}
                        numShields={5/*hands[3].shields*/}
                        top={init + jump * 3}
                        left={0}
                        numStages={stageAreas.length}
                        shield={CardImages.Shield_6}>
                    </PlayerHand>
                }
            </div>
            <div className="decks">
                <img src={CardImages.Back_Adventure} className="deck"></img>
                <img src={CardImages.Back_Story} className="deck"></img>
                <button className="drawButton" onClick={() => drawCard(name, myPlayerID)} style={{ left: "123px" }}>Draw</button>
            </div>

            <div className="questDisplay">
                <QuestDisplay></QuestDisplay>
            </div>

            {/* <Button onClick={() => drawCard(name,0)} >Draw</Button>
            <Button onClick={() => togglePopup()}>End Turn</Button> */}

            {(popup && name === sponsorRequest) &&
                <div id="sponsor-popup">
                    <Popup popupType="SPONSORQUEST" setPopup={setPopup}></Popup>
                </div>
            }
            {(name === sponsorRequest) && isSponsoring &&
                (<div id="finish-setup">
                    <Button
                        onClick={() => {
                            setupComplete(name, myPlayerID, setIsSponsoring);
                        }} style={{}}>Finished Sponsoring
                    </Button>
                </div>)
            }
            {(popup && joinRequest && name !== sponsorRequest) &&
                <div id="join-popup">
                    <Popup popupType="JOINQUEST" setPopup={setPopup}></Popup>
                </div>
            }
            {(name !== sponsorRequest && foeStageStart) &&
                (<div id="finish-setup">
                    <Button
                        onClick={() => {
                            participantSetupComplete(name, myPlayerID);
                        }} style={{}}>Participant Setup Complete
                    </Button>
                </div>)
            }
            {(notifyStageStart) &&
                <div id="foe-stage-start-popup">
                    <Popup popupType="FOESTAGESTART" setPopup={setNotifyStageStart}></Popup>
                </div>
            }
            {(notifyStageEnd) &&
                <div id="foe-stage-end-popup">
                    <Popup popupType="FOESTAGEEND" setPopup={setNotifyStageEnd}></Popup>
                </div>
            }
            {(notifyQuestEnd) &&
                <div id="quest-end-popup">
                    <Popup popupType="QUESTEND" setPopup={setNotifyQuestEnd}></Popup>
                </div>
            }
            {(notifyHandOversize) &&
                <div id="hand-oversize-popup">
                    <Popup popupType="HANDOVERSIZE" setPopup={setNotifyHandOversize}></Popup>
                </div>
            }
            {(notifyHandNotOverSize) &&
                <div id="hand-not-oversize-popup">
                    <Popup popupType="HANDNOTOVERSIZE" setPopup={setNotifyHandNotOversize}></Popup>
                </div>
            }

        </div>
    );
}

export default GameBoard;