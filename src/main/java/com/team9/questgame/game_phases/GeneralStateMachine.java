package com.team9.questgame.game_phases;

import com.team9.questgame.Entities.Players;
import com.team9.questgame.game_phases.utils.StateMachineObserver;
import com.team9.questgame.gamemanager.service.OutboundService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Handle state switch for GeneralGameController
 */
@Component
public class GeneralStateMachine implements StateMachineI<GeneralStateE>, ApplicationContextAware {

    @Autowired
    @Lazy
    private GeneralGameController controller;

    static private ApplicationContext context;
//    @Getter
//    @Autowired
//    private QuestPhaseStateMachine questStateMachine;
    private HashSet<StateMachineObserver<GeneralStateE>> observers;

    @Autowired
    private OutboundService outboundService;

    @Setter
    @Getter
    private GeneralStateE currentState;

    @Getter
    private GeneralStateE previousState;

    @Getter
    @Setter
    private boolean isPhaseEndRequested;

    @Getter
    @Setter
    private boolean isGameStartRequested;

    @Getter
    @Setter
    private boolean isGamePhaseRequested;

    @Getter
    @Setter
    private boolean isHandOversizeRequested;

    public GeneralStateMachine() {
        previousState = null;
        currentState = GeneralStateE.SETUP;
        isGameStartRequested = false;
        observers = new HashSet<>();
    }

    /**
     * Public method for access or set important state
     */

    public boolean isGameStarted() {
        return currentState != GeneralStateE.SETUP && currentState != GeneralStateE.NOT_STARTED;
    }

    public boolean isInPhases() {
        return currentState == GeneralStateE.QUEST_PHASE || currentState == GeneralStateE.TOURNAMENT_PHASE || currentState == GeneralStateE.EVENT_PHASE;
    }

    public boolean isBlocked() {
        return currentState == GeneralStateE.PLAYER_HAND_OVERSIZE;
    }

    /**
     * Switch to the next state if needed
     */
    public void update() {
        GeneralStateE tempState = currentState;

        switch (currentState) {
            case NOT_STARTED:
                currentState = notStartedState();
                break;
            case SETUP:
                currentState = setupState();
                break;
            case DRAW_STORY_CARD:
                currentState = drawStoryCardState();
                break;
            case QUEST_PHASE:
                currentState = questPhaseState();
                break;
            case TOURNAMENT_PHASE:
                currentState = tournamentPhaseState();
                break;
            case EVENT_PHASE:
                currentState = eventPhaseState();
                break;
            case DETERMINING_WINNER:
                currentState = determiningWinnerState();
                break;
            case ENDED:
                currentState = endedState();
                break;
            case PLAYER_HAND_OVERSIZE:
                currentState = playerHandOversizeState();
                break;
            default:
                throw new IllegalStateException("Unknown state: " + currentState);
        }

        if (tempState != currentState) {
            // State changed, update previousState
            this.previousState = tempState;
            notifyObserversStateChanged();
            controller.executeNextAction();
        }

        resetAllRequest();
    }

    private GeneralStateE determiningWinnerState() {
        GeneralStateE nextState;
        if (controller.getWinners().size() == 0) {
            throw new IllegalStateException("No winner found");
        } else if (controller.getWinners().size() == 1 || controller.isEndGameTournamentPlayed()) {
            // Only one winner or end game tournament played
            nextState = GeneralStateE.ENDED;
        } else {
            // More than one winner or end game tournament not played
            nextState = GeneralStateE.DETERMINING_WINNER;
        }
        return nextState;
    }

    private void notifyObserversStateChanged() {
        HashSet<StateMachineObserver<GeneralStateE>> list = new HashSet<>(this.observers);
        for(StateMachineObserver<GeneralStateE> ob : list) {
            ob.observerStateChanged(this.currentState);
        }
    }

    /**
     * The following methods returns the next state of the game to be used by update()
     */
    private GeneralStateE notStartedState() {
        // Always switch to SETUP
        return GeneralStateE.SETUP;
    }

    private GeneralStateE setupState() {
        // Start the game
        GeneralStateE nextState;
        if (!isAllHandNotOversize() || isHandOversizeRequested) {
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        } else if (controller.getPlayers().size() >= GeneralGameController.MIN_PLAYERS && isAllPlayerReady() && isGameStartRequested) {
            nextState = GeneralStateE.DRAW_STORY_CARD;
        } else {
            nextState = GeneralStateE.SETUP;
        }
        return nextState;
    }

    private GeneralStateE drawStoryCardState() {
        GeneralStateE nextState;
//        setPhaseEndRequested(false);
        if (!isAllHandNotOversize() || isHandOversizeRequested) {
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        } else if (this.isWinnerFound()) {
            nextState = GeneralStateE.DETERMINING_WINNER;
        } else if (controller.getStoryCard() != null && isGamePhaseRequested) {
            switch (controller.getStoryCard().getSubType()) {
                case QUEST:
                    nextState = GeneralStateE.QUEST_PHASE;
                    break;
                case EVENT:
                    nextState = GeneralStateE.EVENT_PHASE;
                    break;
                case TOURNAMENT:
                    nextState = GeneralStateE.TOURNAMENT_PHASE;
                    break;
                default:
                    throw new RuntimeException("Unexpected Story Card type");
            }
        } else {
            nextState = GeneralStateE.DRAW_STORY_CARD;
        }

        return nextState;
    }

    private GeneralStateE questPhaseState() {
        GeneralStateE nextState;
        if (!isAllHandNotOversize() || isHandOversizeRequested) {
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        } else if (this.isPhaseEndRequested) {
            nextState = GeneralStateE.DRAW_STORY_CARD;
        } else {
            nextState = GeneralStateE.QUEST_PHASE;
        }
        return nextState;
    }

    private GeneralStateE eventPhaseState() {
        GeneralStateE nextState;
        if (!isAllHandNotOversize() || isHandOversizeRequested) {
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        } else if (this.isPhaseEndRequested) {
            nextState = GeneralStateE.DRAW_STORY_CARD;
        } else {
            nextState = GeneralStateE.EVENT_PHASE;
        }
        return nextState;
    }

    private GeneralStateE tournamentPhaseState() {
        GeneralStateE nextState;
        if (!isAllHandNotOversize() || isHandOversizeRequested) {
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        } else if (this.isPhaseEndRequested) {
            nextState = GeneralStateE.DRAW_STORY_CARD;
        } else {
            nextState = GeneralStateE.TOURNAMENT_PHASE;
        }
        return nextState;
    }

    private GeneralStateE endedState() {
        return GeneralStateE.ENDED;
    }

    private GeneralStateE playerHandOversizeState() {
        GeneralStateE nextState;
        if (isAllHandNotOversize()) {
            // Request to unblock the quest state machine
            // TODO: Do this for tournament as well
//            questStateMachine.setUnblockRequested(true);
//            questStateMachine.update();

            // Let client know and go back to whatever state that was blocked by HAND_OVERSIZE
            outboundService.broadcastHandNotOversize();
            if(isPhaseEndRequested){
                nextState = GeneralStateE.DRAW_STORY_CARD;
            }else{
                nextState = this.previousState;
            }
        } else {
            // Request to block the quest state machine
            // TODO: Do this for tournament as well
//            questStateMachine.setBlockRequested(true);
//            questStateMachine.update();
            nextState = GeneralStateE.PLAYER_HAND_OVERSIZE;
        }

        return nextState;
    }

    /**
     * Private helper methods
     */
    private boolean isWinnerFound() {
        if (controller.getWinners().size() > 0) {
            return true;
        }
        return false;
    }

    private boolean isAllPlayerReady() {
        for (Players p : controller.getPlayers()) {
            if (!p.isReady()) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllHandNotOversize() {
        for(Players p: controller.getPlayers()) {
            if (p.getHand().isHandOversize()) {
                return false;
            }
        }
        return true;
    }

    private void resetAllRequest() {
        setHandOversizeRequested(false);
        setGamePhaseRequested(false);
        setGameStartRequested(false);
        setPhaseEndRequested(false);
    }

    public void registerObserver(StateMachineObserver<GeneralStateE> observer) {
        observers.add(observer);
    }

    public void unregisterObserver(StateMachineObserver<GeneralStateE> observer) {
        observers.remove(observer);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    static public GeneralStateMachine getService() {
        return context.getBean(GeneralStateMachine.class);
    }

}
