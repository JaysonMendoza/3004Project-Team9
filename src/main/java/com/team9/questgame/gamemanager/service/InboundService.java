package com.team9.questgame.gamemanager.service;
import com.team9.questgame.Entities.Players;
import com.team9.questgame.game_phases.GeneralGameController;
import com.team9.questgame.game_phases.tournament.TournamentPhaseController;
import com.team9.questgame.gamemanager.record.socket.PlayerPlayCardInbound;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class InboundService implements ApplicationContextAware {
    private boolean gameStarted;
    private Logger LOG;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private OutboundService outboundService;

    private TournamentPhaseController tournamentController;

    @Autowired
    private GeneralGameController gameController;

    private static ApplicationContext context;

    public InboundService() {
        this.LOG = LoggerFactory.getLogger(InboundService.class);
        this.gameStarted = false;
    }

    /**
     * Start the game
     *
     * @return true if the game is started (or already started before), false otherwise
     */
    public synchronized boolean startGame() {
        final int MIN_PLAYER = 2;
        if (!isGameStarted()) {
            // Attempt to start the game
            if (sessionService.getNumberOfPlayers() >= MIN_PLAYER) {
                boolean isGameAlreadyStarted = isGameStarted();
                if (!isGameAlreadyStarted) {
                    // The first time that the game started
                    setGameStarted(true);
                    for (Players player : sessionService.getPlayerMap().values()) {
                        gameController.playerJoin(player);
                    }
                    gameController.startGame();
                }
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public synchronized void playerDrawStoryCard(String name, long cardId) {
        // Note: cardId is unused
        Players player = sessionService.getPlayerMap().get(name);
        gameController.drawStoryCard(player);
    }

    public synchronized void playerPlayCard(long playerId, long cardId) {
        Players player = sessionService.getPlayerByPlayerId(playerId);
        LOG.info(String.format("Player named %s requested to play a card with id %d", player.getName(), cardId));
        gameController.playerPlayCard(player, cardId);
    }

    public synchronized void playerDiscardCard(String name, long cardId) {
        LOG.info(String.format("Player named %s requested to discard a card with id %d", name, cardId));
        gameController.playerDiscardCard(sessionService.getPlayerMap().get(name), cardId);
    }

    public synchronized void playerActivateCard(long playerId, long cardId) {
        Players player = sessionService.getPlayerByPlayerId(playerId);
        LOG.info(String.format("Player named %s requested to activate a card with id %d", player.getName(), cardId));
        gameController.playerActivateCard(player, cardId);
    }

    public synchronized void playerNotifyHandOversize() {
        LOG.info("NotifyHandOversized sent to Game Controller.");
        // Currently, the gameController doesn't need to know whose hand is oversize
        gameController.handlePlayerHandOversize();
    }

    /**
     * Get called to update the game state
     */
    public synchronized void trigger() {
        gameController.trigger();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public synchronized void tournamentJoinResponse(String name, boolean joined){
        Players player = sessionService.getPlayerMap().get(name);
        if (tournamentController == null) {
            throw new RuntimeException("tournament controller has not been registered");
        }
        tournamentController.checkJoinResult(player, joined);
    }

    public synchronized void tournamentCompetitorSetup(String name){;
        Players player = sessionService.getPlayerMap().get(name);
        if (tournamentController == null) {
            throw new RuntimeException("tournament controller has not been registered");
        }
        tournamentController.checkParticipantSetup(player);
    }

    public void setPhaseEnded(){gameController.requestPhaseEnd();}

    public void registerTournamentPhaseController(TournamentPhaseController c) {
        if (tournamentController != null){
            throw new RuntimeException("Tournament phase controller is already registered and not NULL");
        }
        if (c == null) {
            throw new RuntimeException("Regestering controller is null");
        }
        this.tournamentController = c;
    }

    public void unregisterTournamentPhaseController() {
        this.tournamentController = null;
    }

    public static InboundService getService() {
        return context.getBean(InboundService.class);
    }
}
