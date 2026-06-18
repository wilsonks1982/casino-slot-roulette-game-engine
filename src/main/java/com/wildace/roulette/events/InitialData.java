package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InitialData {
    TableInfo tableInfo;
    Payout payout;
    TableLimit tableLimit;
    Session session;
    Wallet wallet;
    GameState gameState;
    CurrentBets currentBets;
    History history;

}
