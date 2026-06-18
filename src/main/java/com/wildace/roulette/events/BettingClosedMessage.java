package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BettingClosedMessage {
    String type;
    String messageId;
    String timestamp;
    BettingClosed payload;
}
