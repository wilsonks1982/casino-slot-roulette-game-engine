package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BettingOpened {
    String roundId;
    Integer duration;
}
