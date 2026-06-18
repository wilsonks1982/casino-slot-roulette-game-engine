package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PayoutCompletedMessage {
    String type;
    String messageId;
    String timestamp;
    PayoutCompleted payload;
}
