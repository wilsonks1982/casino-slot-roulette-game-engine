package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BettingClosed {
    String roundId;
    Double totalBets;
}
