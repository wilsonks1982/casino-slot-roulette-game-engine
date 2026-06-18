package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Group {
    Double groupFirstLine;
    Double groupSecondLine;
    Double groupThirdLine;
    Double group1to12;
    Double group13to24;
    Double group25to36;
    Double groupBlack;
    Double groupRed;
    Double groupOdd;
    Double groupEven;
    Double group1to18;
    Double group19to36;
}
