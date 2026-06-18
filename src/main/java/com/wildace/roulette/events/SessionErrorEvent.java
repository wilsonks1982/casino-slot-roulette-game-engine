package com.wildace.roulette.events;

public record SessionErrorEvent(
        String sessionId,
        String errorMessage
) {
}