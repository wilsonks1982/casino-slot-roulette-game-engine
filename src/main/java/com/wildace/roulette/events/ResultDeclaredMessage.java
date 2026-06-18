package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ResultDeclaredMessage {
    String type;
    String messageId;
    String timestamp;
    ResultDeclared payload;
}
