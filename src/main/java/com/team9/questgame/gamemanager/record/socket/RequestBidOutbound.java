package com.team9.questgame.gamemanager.record.socket;

import com.team9.questgame.Data.PlayerData;

import java.util.ArrayList;

public record RequestBidOutbound(PlayerData player, int maxBid, PlayerData maxBidPlayer) {
}
