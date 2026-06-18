package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PayoutCompleted {
    String roundId;
    Double totalStake;
    Double totalPayout;
    Double netResult;
}
