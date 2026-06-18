package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Session {
    String sessionId;
    String egmId;
    String uid;
    String protocolVersion = "1.0";
    Integer heartbeatInterval = 30; // in seconds
}
