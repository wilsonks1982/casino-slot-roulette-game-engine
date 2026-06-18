package com.wildace.roulette.events;


import lombok.Value;

@Value
public class UserBalanceUpdatedEvent {
    String uid;
    String roundId;     // may be null if not applicable
    Double newBalance;
    String reason;      // human readable string
}