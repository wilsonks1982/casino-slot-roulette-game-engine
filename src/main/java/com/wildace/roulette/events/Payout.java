package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Payout {
    Integer straightUpBet = 35; ;
    Integer splitBet = 17;
    Integer streetBet = 11;
    Integer cornerBet = 8;
    Integer basketBet = 6;
    Integer lineBet = 5;
    Integer columnBet = 2;
    Integer dozenBet = 2;
    Integer outsideBet = 1;
}
