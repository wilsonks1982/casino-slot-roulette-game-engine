package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class RoundCreatedMessage {
    String type;
    String messageId;
    String timestamp;
    RoundCreated payload;
}
