package com.team9.questgame.game_phases.tournament;

import com.team9.questgame.Data.PlayerData;
import com.team9.questgame.Entities.Effects.EffectResolverService;
import com.team9.questgame.Entities.Players;
import com.team9.questgame.Entities.cards.StoryCards;
import com.team9.questgame.Entities.cards.TournamentCards;
import com.team9.questgame.exception.IllegalGameRequest;
import com.team9.questgame.exception.IllegalQuestPhaseStateException;
import com.team9.questgame.game_phases.GamePhases;
import com.team9.questgame.game_phases.GeneralGameController;
import com.team9.questgame.game_phases.GeneralStateE;
import com.team9.questgame.game_phases.GeneralStateMachine;
import com.team9.questgame.game_phases.event.EventPhaseController;
import com.team9.questgame.game_phases.quest.QuestPhaseStatesE;
import com.team9.questgame.game_phases.utils.PlayerTurnService;
import com.team9.questgame.game_phases.utils.StateMachineObserver;
import com.team9.questgame.gamemanager.record.socket.TournamentPlayersOutbound;
import com.team9.questgame.gamemanager.service.OutboundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class TournamentPhaseController implements GamePhases<TournamentCards,TournamentPhaseStatesE>, StateMachineObserver<GeneralStateE> {
    private final Logger LOG;
    private final GeneralGameController gameController;
    private TournamentCards card;
    private TournamentPhaseStatesE state;
    private Map<Players, Integer> competitors;
    private ArrayList<Players> winners;
    private PlayerTurnService turnService;
    private int joinAttempts;
    private int participantSetupResponses;
    private boolean isTiebreaker;
    private int oldCompetitorOffset;

    public TournamentPhaseController(GeneralGameController gameController, TournamentCards card) {
        LOG = LoggerFactory.getLogger(EventPhaseController.class);
        GeneralStateMachine.getService().registerObserver(this);
        competitors = new HashMap<>();
        winners = new ArrayList<>();
        this.gameController = gameController;
        this.card=card;
        joinAttempts = 0;
        participantSetupResponses = 0;
        isTiebreaker = false;
        oldCompetitorOffset = 0;
    }

    public boolean receiveCard(TournamentCards card){
        if(card == null || state!=TournamentPhaseStatesE.READY) {
            return false;
        }
        if(this.card!=null) {
            LOG.warn("Tournament Phase Controller already has a tournament card");
        }
        this.card = card;
        nextState();
        return true;
    }


    @Override
    public TournamentPhaseStatesE getCurrState() {
        return state;
    }

    @Override
    public void startPhase(PlayerTurnService playerTurnService) {
        if(this.state != TournamentPhaseStatesE.READY) {
            LOG.error("The Tournament Phase Controller is in state " +
                    state + "when it should be in state READY. Returning");
            return;
        }
        turnService = playerTurnService;
        nextState();

    }
    /**
     * Player's decision to join a Tournament or not
     * @param player the player who sent this request
     * @param joined true if they want to join this stage, false otherwise
     */
    public void checkJoinResult(Players player, boolean joined){
        if (state != TournamentPhaseStatesE.JOIN) {

        } else if (competitors.containsValue(player)) {
            throw new IllegalGameRequest("This player is already in the tournament", player);
        }

        // Increment this counter so that the stateMachine knows when all players replied
        this.joinAttempts++;

        if(joined){
            competitors.put(player, player.getPlayArea().getBattlePoints() * -1);
            player.getPlayArea().setPlayerTurn(true);
        }

        if(joinAttempts == turnService.getPlayers().size()) {
            //if only one person joined, go to resolution
            if(competitors.size() == 1){
                this.state = TournamentPhaseStatesE.PLAYER_SETUP;
            }
            nextState();
        }
    }

    /**
     * Deal an adventure card to each competitor
     */
    private void dealAdventureCard(){
        for(Players player : competitors.keySet()){
            gameController.dealCard(player);
        }
        nextState();
    }

    public void tiebreakerSetup(){
        oldCompetitorOffset = competitors.size() - winners.size();
        competitors.clear();
        for(Players player : winners){
            competitors.put(player, player.getPlayArea().getBattlePoints() * -1);
            player.getPlayArea().setPlayerTurn(true);
        }
        nextState();
    }

    /**
     * A Tournament participant informs that their stage setup is complete
     * their battlepoints(minus already existing ally cards points) are recorded
     */
    public void checkParticipantSetup(Players player){
        if (state != TournamentPhaseStatesE.PLAYER_SETUP) {

        }
        participantSetupResponses++;
        player.getPlayArea(). setPlayerTurn(false);
        competitors.put(player,
                competitors.get(player) + player.getPlayArea().getBattlePoints());
        if(participantSetupResponses == competitors.size()) {
            nextState();
        }
    }

    /**
     * A Tournament participant informs that their stage setup is complete
     */
    public void resolveTournament(){
        int max = Collections.max(competitors.values());
        for(Players competitor : competitors.keySet()){
            if(competitors.get(competitor) == max){
                winners.add(competitor);
            }
        }
        nextState();
    }

    /**
     * Distribute rewards to winner(s)
     */
    public void distributeRewards(){
        if(winners.size() > 1 && !isTiebreaker){
            this.state = TournamentPhaseStatesE.TIEBREAKER;
            nextState();
        }
        int rewards = card.getBonusShields() + competitors.size() + oldCompetitorOffset;
        HashMap<Players, Integer> participantRewards = new HashMap<>();
        for(Players player : winners){
            participantRewards.put(player, rewards);
        }
        EffectResolverService.getService().playerAwardedShields(participantRewards);
    }

    @Override
    public void endPhase() {
        onGameReset();
        OutboundService.getService().broadcastTournamentPhaseEnded(
                new TournamentPlayersOutbound(getPlayerData())
        );

    }

    private ArrayList<PlayerData> getPlayerData(){
        ArrayList<PlayerData> winnerData = new ArrayList<>();
        for(Players player : winners){
            winnerData.add(player.generatePlayerData());
        }
        return winnerData;
    }

    public void onGameReset() {
        this.state = TournamentPhaseStatesE.READY;
        if(card!=null) {
            card.discardCard();
            this.card = null;
        }
        joinAttempts = 0;
        participantSetupResponses = 0;
        competitors.clear();
        winners.clear();
        isTiebreaker = false;
    }



    public void nextState(){
        switch(state){
            case READY -> {
                this.state = TournamentPhaseStatesE.JOIN;
                OutboundService.getService().broadcastTournamentPhaseStart();
            }
            case JOIN -> {
                this.state = TournamentPhaseStatesE.DRAW_CARD;
                dealAdventureCard();
            }
            case DRAW_CARD -> {
                this.state = TournamentPhaseStatesE.PLAYER_SETUP;
                OutboundService.getService().broadcastTournamentSetup();
            }
            case PLAYER_SETUP -> {
                this.state = TournamentPhaseStatesE.RESOLUTION;
                resolveTournament();
            }
            case RESOLUTION -> {
                this.state = TournamentPhaseStatesE.REWARDS;
                distributeRewards();
            }
            case REWARDS -> {
                this.state = TournamentPhaseStatesE.ENDED;
                endPhase();
            }
            case TIEBREAKER -> {
                this.state = TournamentPhaseStatesE.DRAW_CARD;
                isTiebreaker = true;
                tiebreakerSetup();
            }

        }
    }




    @Override
    public TournamentCards getCard() {
        return card;
    }

    @Override
    public void observerStateChanged(GeneralStateE newState) {
        //TODO: finish handling oversized hands
//        if(newState==GeneralStateE.PLAYER_HAND_OVERSIZE) {
//            this.previousState = this.currentState;
//            this.currentState = QuestPhaseStatesE.BLOCKED;
//            LOG.info(String.format("Moved from state %s to state %s", previousState, currentState));
//        }
//        else if(this.currentState==QuestPhaseStatesE.BLOCKED) {
//            this.currentState = this.previousState;
//            this.previousState = QuestPhaseStatesE.BLOCKED;
//            update();
//        }
    }
}
