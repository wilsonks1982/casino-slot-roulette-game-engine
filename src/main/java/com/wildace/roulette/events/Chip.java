package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Chip {
    String id;
    String color;
    Integer value;
    String img;
    Boolean defaultChip;
}
