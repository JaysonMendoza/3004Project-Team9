package com.team9.questgame.game_phases.quest;

import com.team9.questgame.ApplicationContextHolder;
import com.team9.questgame.Data.CardData;
import com.team9.questgame.Data.PlayerData;
import com.team9.questgame.Data.PlayerRewardData;
import com.team9.questgame.Entities.Effects.EffectResolverService;
import com.team9.questgame.Entities.Players;
import com.team9.questgame.Entities.cards.*;
import com.team9.questgame.exception.IllegalGameRequest;
import com.team9.questgame.exception.SponsorAlreadyExistsException;
import com.team9.questgame.game_phases.GamePhaseControllers;
import com.team9.questgame.game_phases.GamePhases;
import com.team9.questgame.game_phases.GeneralGameController;
import com.team9.questgame.exception.IllegalQuestPhaseStateException;
import com.team9.questgame.game_phases.utils.PlayerTurnService;
import com.team9.questgame.gamemanager.service.QuestPhaseOutboundService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;

@Component
public class QuestPhaseController implements GamePhaseControllers {
    Logger LOG;
    @Getter
    @Autowired
    private QuestPhaseStateMachine stateMachine;
    @Getter
    private QuestCards questCard;

    @Autowired
    @Lazy
    private GeneralGameController generalController;

    @Autowired
    private QuestPhaseOutboundService outboundService;
    @Autowired
    @Lazy
    private EffectResolverService effectResolverService;
    @Getter
    private ArrayList<Players> questingPlayers;
    @Getter
    private PlayerTurnService playerTurnService;
    private PlayerTurnService joinTurnService;
    @Getter
    private ArrayList<StagePlayAreas> stages;
    private StagePlayAreas newStage;
    @Getter
    private Players sponsor;
    @Getter
    private int sponsorAttempts;
    @Getter
    private int joinAttempts;
    @Getter
    private int numParticipants;
    @Getter
    private int participantSetupResponses;
    @Getter
    private int curStageIndex;
    private boolean minJoin;
    @Getter
    private boolean stagesAreValid;


    public QuestPhaseController() {
        LOG = LoggerFactory.getLogger(QuestPhaseController.class);
        this.questingPlayers = new ArrayList<>();
        this.stages = new ArrayList<>();
        playerTurnService = null;
        sponsor = null;
        sponsorAttempts = 0;
        this.outboundService = ApplicationContextHolder.getContext().getBean(QuestPhaseOutboundService.class);
        numParticipants = 0;
        minJoin = false;
        stagesAreValid = false;
    }

    /**
     * Receive the QuestCard from the GeneralGameController
     * @param card
     * @return
     */
    public boolean receiveCard(QuestCards card) {
        if(stateMachine.getCurrentState() != QuestPhaseStatesE.NOT_STARTED){
            // Quest can only receive questCard if no quest is currently in progress
            return false;
        } else if (stateMachine.isBlocked()) {
            return false;
        }

        questCard = card;

        stateMachine.update();
        return true;
    }

    /**
     * Starts the quest phase
     * Assume that the phase has already been reset (NOT_STARTED state)
     * @param playerTurnService A turn service in which the first turn is the player who drawn the Quest Card
     */
    @Override
    public void startPhase(PlayerTurnService playerTurnService) {
        if (stateMachine.isQuestStarted()) {
            throw new IllegalQuestPhaseStateException("Cannot start quest phase when the Quest has already started");
        } else if (questCard == null) {
            throw new RuntimeException("Cannot start quest phase, questCard is null");
        } else if (stateMachine.isBlocked()) {
            throw new IllegalGameRequest("Cannot start phase when the game is blocked", playerTurnService.getPlayerTurn());
        }

        stateMachine.setPhaseStartRequested(true);
        stateMachine.update();
        if (stateMachine.getCurrentState() == QuestPhaseStatesE.QUEST_SPONSOR) {
            LOG.info("Quest phase started");
            this.playerTurnService = playerTurnService;
            registerPlayerPlayAreas(questCard);
            outboundService.broadcastSponsorSearch(playerTurnService.getPlayerTurn().generatePlayerData());
        }
    }


    /**
     * Check client sponsor result
     * @param player the player who made the sponsor request
     * @param found true if the player decides to sponsor(sponsor found) false otherwise
     */
    public void checkSponsorResult(Players player, boolean found){
        if(stateMachine.getCurrentState() != QuestPhaseStatesE.QUEST_SPONSOR){
            throw new IllegalQuestPhaseStateException("Cannot check sponsor result when not in QUEST_SPONSOR state" );
        } else if (player.getPlayerId() != playerTurnService.getPlayerTurn().getPlayerId()) {
            throw new IllegalGameRequest("Must be current player turn to send sponsor result", player);
        } else if (stateMachine.isBlocked()) {
            throw new IllegalGameRequest("Cannot check sponsor when the game is blocked", player);
        }

        // Increment sponsorAttempts counter so that the state machine knows when we run out of attempts
        this.sponsorAttempts++;

        if(found){
            if(this.sponsor != null){
                throw new SponsorAlreadyExistsException(player);
            } else if(validateSponsor(player)){
                this.sponsor = player;
            }

        }
        
        stateMachine.update();
        switch (stateMachine.getCurrentState()) {
            case QUEST_SPONSOR -> {
                // Attempts sponsor search on the next player
                playerTurnService.nextPlayer();
                outboundService.broadcastSponsorSearch(playerTurnService.getPlayerTurn().generatePlayerData());
            } case QUEST_SETUP -> {
                outboundService.broadcastSponsorFound(sponsor.generatePlayerData());
                this.setupStage();
            } case ENDED -> {
                this.endPhase();
            } default -> {
                throw new IllegalQuestPhaseStateException("Unknown state");
            }
        }
    }

    /**
     * Called by checkSponsorResult() after a sponsor is found and the quest is set up for the first time
     */
    private void setupStage(){
        //old way
//        newStage = new StagePlayAreas(questCard, sponsor, stages.size());
//        sponsor.getPlayArea().onPlayAreaChanged(newStage);
//        outboundService.broadcastSponsorSetup(sponsor.generatePlayerData());

        //make as many stages as are stages in the quest and add them to the array
        this.sponsor.getPlayArea().onPhaseNextPlayerTurn(this.sponsor);
        for(int i = 0; i < questCard.getStages(); i++){
            stages.add(new StagePlayAreas(questCard, sponsor,i));
        }
        stateMachine.update();
    }

    /**
     *
     * Sponsor informs that the quest setup is complete
     * @param player the player who sent the request
     * @return true if the setup is accepted, false otherwise
     */
    public boolean questSetupComplete(Players player){
        if(stateMachine.getCurrentState() != QuestPhaseStatesE.QUEST_SETUP){
            throw new IllegalQuestPhaseStateException("Cannot set up a stage when not in QUEST_SETUP phase");
        } else if (player.getPlayerId() == this.sponsor.getPlayerId()) {
            throw new IllegalGameRequest("Only the sponsor can setup the quest", player);
        } else if(stateMachine.isBlocked()){
            throw new IllegalGameRequest("Cannot proceed when the quest phase is blocked", player);
        }

        // TODO: pass in an array of new StagesPlayArea to the validateStageSetup function
        if (!validateStageSetup(stages)) {
            return false;
        }
        stagesAreValid = true;
        // TODO: set the quest stages
        // this.stages = ...
        stateMachine.update();

        switch (stateMachine.getCurrentState()) {
            case QUEST_SETUP -> {
                outboundService.broadcastSponsorFound(sponsor.generatePlayerData());
                this.setupStage();
            } case QUEST_JOIN -> {
                this.checkJoins();
            } default -> {
                throw new IllegalQuestPhaseStateException("Unknown state");
            }
        }
        return true;
    }

    /**
     * Used by stageSetupComplete() Validate if the new stages' setup are valid
     * @param newStages
     * @return
     */
    private boolean validateStageSetup(ArrayList<StagePlayAreas> newStages) {

        // Check if subsequent stages have increasing battlePoint
        int minBattlePoint = 0;
        for (StagePlayAreas stage: newStages) {
            if (stage.getBattlePoints() < minBattlePoint) {
                return false;
            }
            minBattlePoint++;
        }

        // Check if each stage contain exactly 1 foe and unique weapons
        for (StagePlayAreas stage: newStages) {
            ArrayList<FoeCards> foeCards = new ArrayList<>();
            ArrayList<WeaponCards> weaponCards = new ArrayList<>();

            HashMap<AllCardCodes, AdventureCards> allCards = stage.getAllCards();
            for (AdventureCards card: allCards.values()) {
                if (card.getSubType() == CardTypes.FOE) {
                    foeCards.add((FoeCards) card);
                } else if (card.getSubType() == CardTypes.QUEST) {
                    weaponCards.add((WeaponCards) card);
                }
            }
            if (foeCards.size() != 1) {
                // There must be exactly 1 foe in the stage
                return false;
            } else {
                // All weapon cards must be unique
                for (WeaponCards thisCard: weaponCards) {
                    for (WeaponCards otherCard: weaponCards) {
                        if (thisCard.getCardID() != otherCard.getCardID() && thisCard.getCardName() == otherCard.getCardName()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Called by stageSetup to ask players to join a Quest Stage
     */
    private void checkJoins(){
        if(joinAttempts == 0){ playerTurnService.setPlayerTurn(sponsor);
            playerTurnService.nextPlayer();
        }
        Players player = playerTurnService.getPlayerTurn();
        outboundService.broadcastJoinRequest(player.generatePlayerData());
        playerTurnService.nextPlayer();
        this.joinAttempts++;

    }

    /**
     * Player's decision to join a quest stage or not
     * @param player the player who sent this request
     * @param joined true if they want to join this stage, false otherwise
     */
    public void checkJoinResult(Players player, boolean joined){
        if (stateMachine.getCurrentState() != QuestPhaseStatesE.QUEST_JOIN) {
            throw new IllegalQuestPhaseStateException("A player can only join when the quest is in QUEST_JOIN state");
        } else if (player.getPlayerId() == sponsor.getPlayerId()) {
            throw new IllegalGameRequest("The sponsor cannot join quest stages", player);
        }

        if(joined){
            minJoin = true;
            questingPlayers.add(player);
            if(joinAttempts == playerTurnService.getPlayers().size()-1){
                joinTurnService = new PlayerTurnService(questingPlayers);
            }
        }

        stateMachine.update();
        switch (stateMachine.getCurrentState()) {
            case QUEST_JOIN -> {
                this.checkJoins();
            } case PARTICIPANT_SETUP -> {
                curStageIndex=0;
                participantSetup();
                //resolveStage(0);
            } case ENDED -> {
            } default -> {
                throw new IllegalQuestPhaseStateException("Unknown state");
            }
        }
    }

    private void participantSetup(){
        dealAdventureCard();
        outboundService.broadcastFoeStageStart(generateQuestorData());
    }

    /**
     * A quest participant informs that their stage setup is complete
     */
    public void checkParticipantSetup(){
        if (stateMachine.getCurrentState() != QuestPhaseStatesE.PARTICIPANT_SETUP) {
            throw new IllegalQuestPhaseStateException("A player can only setup their area when the quest is in PARTICIPANT_SETUP state");
        }

        participantSetupResponses++;
        stateMachine.update();
        switch (stateMachine.getCurrentState()){
            case PARTICIPANT_SETUP -> {
                return;
            } case STAGE_ONE -> {
                resolveStage(0);
            } case STAGE_TWO -> {
                resolveStage(1);
            } case STAGE_THREE -> {
                resolveStage(2);
            } case ENDED -> {
                endPhase();
            } default -> {
                throw new IllegalQuestPhaseStateException("Unknown state");
            }
        }
    }

    private void resolveStage(int stageNum){
        for(Players player:questingPlayers){
            if(player.getPlayArea().getBattlePoints() < stages.get(stageNum).getBattlePoints()){
                questingPlayers.remove(player);

            }
            player.getPlayArea().discardAllWeapons();
        }

        outboundService.broadcastStageResult(generateQuestorData());

        participantSetupResponses=0;
        curStageIndex++;
        stateMachine.update();
        switch (stateMachine.getCurrentState()){
            case PARTICIPANT_SETUP -> {
                participantSetup();
            } case ENDED -> {
                endPhase();
            } default ->{
                throw new IllegalQuestPhaseStateException("Unknown state");
            }
        }
    }

    /**
     * Helper function to generate player data on questing players
     */
    private ArrayList<PlayerData> generateQuestorData(){
        ArrayList<PlayerData> questorData = new ArrayList<>();
        for(Players player : questingPlayers){
            questorData.add(player.generatePlayerData());
        }
        return questorData;
    }

    /**
     * Reset the phase
     */
    public void endPhase() {
        if (stateMachine.getCurrentState() != QuestPhaseStatesE.ENDED) {
            throw new IllegalQuestPhaseStateException("Cannot end phase when it's not in ENDED state");
        }

        if(questingPlayers.size() == 0 && sponsor != null && minJoin){
            distributeRewards();
            // TODO: broadcast everyone failed quest
        }else if(sponsor != null && !minJoin){
            // TODO: broadcast no one joined quest, so quest ended
        }else {
            //TODO: broadcast no sponsor found
        }

        this.questCard.discardCard();
        this.questCard = null;
        this.stages.clear();
        stateMachine.setPhaseReset(true);
        stateMachine.update();
    }

    /**
     * Reset the game
     */
    public void onGameReset() {

    }

    @Override
    public boolean cardPlayRequest(Cards card){return false;}

    private void registerPlayerPlayAreas(QuestCards card){
        for(Players player : playerTurnService.getPlayers()){
            player.getPlayArea().registerGamePhase(this);
            player.getPlayArea().onQuestStarted(card);
        }
    }

    @Override
    public PlayerRewardData getRewardData(){
        return null;
    }

    private void distributeRewards(){

        HashMap<Players, Integer> participantRewards = new HashMap<>();
        for(Players player : questingPlayers){
            participantRewards.put(player, questCard.getStages());
        }
        effectResolverService.onQuestCompleted(participantRewards);
        int sumCards = 0;
        HashMap<Players, Integer> sponsorRewards = new HashMap<>();

        for(StagePlayAreas stage : stages){
            sumCards += stage.size();
        }
        sponsorRewards.put(sponsor, sumCards+questCard.getStages());
        effectResolverService.drawAdventureCards(sponsorRewards);
    }

    @Override
    public StoryDeckCards getPhaseCardCode(){
        return null;
    }

    private void dealAdventureCard(){
        for(Players player : questingPlayers){
            generalController.dealCard(player);
        }
    }

    public void sponsorPlayCard(Players player, long cardId, int src, int dst){
        if(!stateMachine.isInQuest()){
            throw new IllegalQuestPhaseStateException("Must be quest setup state to change stages");
        } else if(player.getPlayerId() != sponsor.getPlayerId()){
            throw new RuntimeException("Only the sponsor can play cards during quest setup");
        }

        if(src < 0 && dst < 0){
            //play card to playerPlayArea
            //player.actionPlayCard(cardId);
            throw new UnsupportedOperationException("Not implemented");
        }else if (src < 0){

            //play card from hand to specific stage
            player.getPlayArea().onPlayAreaChanged(stages.get(dst));
            //if player plays nonFOE/WEAPON card it'll be played into their playerplayarea
            player.actionPlayCard(cardId);

            player.getPlayArea().onPlayAreaChanged(null);

        } else if( dst < 0){
            //withdraw card from stage to hand?
            stages.get(src).returnToHand(cardId);
        } else{
            //move card from stage to stage
            //stages.get((int)src).onPlayAreaChanges(stages.get((int)dst));
            stages.get(src).playCard(cardId, stages.get(dst));
        }
    }

    /**
     * Validate if a player can sponsor the quest
     * @param player
     * @return
     */
    private boolean validateSponsor(Players player){
        return true;
    }

}
