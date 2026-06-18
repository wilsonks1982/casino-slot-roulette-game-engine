package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TableInfo {
    String tableId;
    String tableName;
    String wheelType; // Could be "European", "American", etc.
}
