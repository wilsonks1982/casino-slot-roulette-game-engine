package com.wildace.roulette.services;

import org.springframework.stereotype.Component;

@Component
public class BetIdGenerator {
    public String generate(String uid, String roundId) {
        return "bet-" + uid + "-" + roundId + "-" + System.currentTimeMillis();
    }
}
