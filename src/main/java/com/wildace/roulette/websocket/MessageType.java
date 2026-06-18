package com.wildace.roulette.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
    @JsonProperty("PLACE_BET")
    PLACE_BET,

    @JsonProperty("HEARTBEAT")
    HEARTBEAT
}