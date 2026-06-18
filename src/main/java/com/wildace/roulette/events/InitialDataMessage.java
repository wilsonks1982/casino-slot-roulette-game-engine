package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InitialDataMessage {
    String type;
    String messageId;
    String timestamp;
    InitialData payload;
}
