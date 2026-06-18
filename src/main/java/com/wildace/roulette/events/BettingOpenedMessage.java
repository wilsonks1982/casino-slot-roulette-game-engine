package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BettingOpenedMessage {
    String type;
    String messageId;
    String timestamp;
    BettingOpened payload;
}
