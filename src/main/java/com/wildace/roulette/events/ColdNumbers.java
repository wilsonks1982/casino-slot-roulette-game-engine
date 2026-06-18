package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class ColdNumbers {
    List<Integer> coldNumbers;
}
