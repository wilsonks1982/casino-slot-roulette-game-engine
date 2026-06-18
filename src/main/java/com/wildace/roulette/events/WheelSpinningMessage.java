package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class WheelSpinningMessage {
    String type;
    String messageId;
    String timestamp;
    WheelSpinning payload;
}
