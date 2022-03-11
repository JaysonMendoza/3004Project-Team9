package com.team9.questgame.game_phases.utils;

import com.team9.questgame.Entities.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PlayerTurnServiceTest {

    private PlayerTurnService playerTurnService;
    private ArrayList<Players> players;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>(Arrays.asList(new Players("A"),
                  new Players("B"),
                  new Players("C"),
                  new Players("D")));
        playerTurnService = new PlayerTurnService(players);
    }

    @Test
    void nextPlayerTurn() {
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("A");
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("B");
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("C");
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("D");
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("A");
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("B");
        assertThat(playerTurnService.getPlayerTurn().getName()).isEqualTo("B");
    }

    @Test
    void nextPlayerIndexTurn() {
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(0);
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(1);
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(2);
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(3);
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(0);
        playerTurnService.nextPlayer();
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(1);
        assertThat(playerTurnService.getPlayerIndexTurn()).isEqualTo(1);
    }

    @Test
    void setPlayerTurn() {
        assertThat(playerTurnService.setPlayerTurn(-1)).isFalse();
        assertThat(playerTurnService.setPlayerTurn(0)).isTrue();
        assertThat(playerTurnService.setPlayerTurn(1)).isTrue();
        assertThat(playerTurnService.setPlayerTurn(2)).isTrue();
        assertThat(playerTurnService.setPlayerTurn(3)).isTrue();
        assertThat(playerTurnService.setPlayerTurn(4)).isFalse();

        assertThat(playerTurnService.setPlayerTurn(new Players("E"))).isFalse();
        assertThat(playerTurnService.setPlayerTurn(players.get(0))).isTrue();
        assertThat(playerTurnService.setPlayerTurn(players.get(1))).isTrue();
        assertThat(playerTurnService.setPlayerTurn(players.get(2))).isTrue();
        assertThat(playerTurnService.setPlayerTurn(players.get(3))).isTrue();

    }
}