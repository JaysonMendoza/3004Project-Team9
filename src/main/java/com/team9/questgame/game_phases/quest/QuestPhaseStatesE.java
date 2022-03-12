package com.team9.questgame.game_phases.quest;

public enum QuestPhaseStatesE {
    NOT_STARTED,
    QUEST_SPONSOR, //check for sponsor
    QUEST_SETUP, //sponsor sets up stages
    STAGE_ONE, //stages play out in order
    STAGE_TWO,
    STAGE_THREE,
    ENDED
}
