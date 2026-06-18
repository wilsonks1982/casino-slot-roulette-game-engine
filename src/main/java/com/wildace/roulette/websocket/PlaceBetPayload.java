package com.wildace.roulette.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wildace.roulette.events.Bet;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaceBetPayload(
        @JsonProperty("roundId")
        @NotNull
        String roundId,

        @JsonProperty("stake")
        @NotNull
        Integer stake,

        @JsonProperty("betsList")
        @NotNull
        List<Bet> betsList
) {}