package com.wildace.roulette.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GameMessage(
        @JsonProperty("type")
        @NotNull(message = "Message type is required")
        MessageType type,

        @JsonProperty("payload")
        Object payload
) { }
