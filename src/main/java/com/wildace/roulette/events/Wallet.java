package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Wallet {
    Double balance;

    @Builder.Default
    String currency = "INR";
}