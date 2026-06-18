package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BallReleasedMessage {
    String type;
    String messageId;
    String timestamp;
    BallReleased payload;
}
