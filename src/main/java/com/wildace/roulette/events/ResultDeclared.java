package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Builder
@Value
public class ResultDeclared {
    String roundId;
    Integer winningNumber;
    String color;
    String parity;
    Integer dozen;
    Integer column;
    List<Integer> hotNumbers;
    List<Integer> coldNumbers;
    List<Stat> statistics;
    Map<String, Double> groups;
    List<Win> lastNumbers;
}
