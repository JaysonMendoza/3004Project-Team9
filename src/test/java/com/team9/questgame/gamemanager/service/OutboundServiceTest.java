package com.team9.questgame.gamemanager.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OutboundServiceTest {
    @Autowired
    OutboundService outboundService;

    @Test
    void contextLoad() {
        assertThat(outboundService).isNotNull();
    }

    @Test
    void broadcastPlayerConnect() {
    }

    @Test
    void broadcastPlayerDisconnect() {
    }

    @Test
    void broadcastGameStart() {
    }

    @Test
    void broadcastNextTurn() {
    }

    @Test
    void broadcastPlayerChanged() {
    }
}