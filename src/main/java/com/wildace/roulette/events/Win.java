package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Win {
    Integer winningNumber;
    String roundId;
}
