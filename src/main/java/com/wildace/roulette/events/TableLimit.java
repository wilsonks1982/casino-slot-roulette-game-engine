package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class TableLimit {
    List<Chip> chips = List.of(
            Chip.builder().id("1").color("red").value(10).img("chip_red.png").defaultChip(true).build(),
            Chip.builder().id("2").color("blue").value(50).img("chip_blue.png").defaultChip(false).build(),
            Chip.builder().id("3").color("green").value(100).img("chip_green.png").defaultChip(false).build(),
            Chip.builder().id("4").color("black").value(500).img("chip_black.png").defaultChip(false).build(),
            Chip.builder().id("5").color("purple").value(1000).img("chip_purple.png").defaultChip(false).build()
    );

    Integer minBet = 10;
    Integer maxBet = 100000; //1 lakh
    Integer minSideBet = 0;
    Integer maxSideBet = 0;
    Integer minStraightUpBet = 10;
    Integer maxStraightUpBet = 25000;
    Integer minSplitBet = 10;
    Integer maxSplitBet = 50000;
    Integer minStreetBet = 10;
    Integer maxStreetBet = 75000;
    Integer minCornerBet = 10;
    Integer maxCornerBet = 100000;
    Integer minLineBet = 100;
    Integer maxLineBet = 150000;
    Integer minDozenColumnBet = 100;
    Integer maxDozenColumnBet = 300000;
    Integer minOutsideBet = 100;
    Integer maxOutsideBet = 500000;
}
