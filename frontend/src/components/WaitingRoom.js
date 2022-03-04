import React, { useEffect} from "react";
import { startGame } from "../ClientSocket";
import { 
        useName, 
        useConnected, 
        useLoadPlayers,
        usePlayers, 
        useSetPlayers,
        useGameStarted
       } from "../Store";

const WaitingRoom = () => {
    const name = useName();
    var players = usePlayers();
    const loadPlayers = useLoadPlayers();
    const setPlayers = useSetPlayers();
    const connected = useConnected();
    const gameStarted = useGameStarted();
    useEffect(() => {
                loadPlayers(setPlayers);
            
        }, [connected]
    )
    
    if(!(Array.isArray(players))){
        players = Object.keys(players);
    }


    const test=(e) =>{
        startGame();
    };
    return (
        <div className="WaitingRoom">
            <h2>Welcome to the pregame lobby, {name}</h2>
            {players.length >= 2 ? (
                <>
                <p>There Are Enough Players to Begin: {players.length}/4</p>
                <button
                onClick={test}
                className="btn btn-primary"
                >Ready
                </button>
                </>
                ) : (
                    <>
                    <p> Not Enough Players! only {players.length} / 4</p>               
                    <button
                    disabled={true}
                    onClick={test}
                    className="btn btn-primary"
                    >Ready
                    </button>
                    </>
                )}
                <h3>Players in lobby</h3>
                <ul className="list-players">
                    {players.map((player, index) => (
                        <li key={index}>
                            {player}
                        </li>
                    ))}
                </ul>
            
        </div>
    );

};
export default WaitingRoom;