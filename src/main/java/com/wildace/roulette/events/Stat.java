package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Stat {
    Integer count;
    Double percentage;
}
