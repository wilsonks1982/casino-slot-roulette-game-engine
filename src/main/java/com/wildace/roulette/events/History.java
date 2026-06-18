package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class History {
    HotNumbers hotNumbers;
    ColdNumbers coldNumbers;
    Statistics statistics;
    Group group;
    LastNumbers lastNumbers;
}
