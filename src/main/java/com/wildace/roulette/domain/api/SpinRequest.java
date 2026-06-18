/*
 * Copyright 2024 Wildace Private Limited - All Rights Reserved
 *
 * Licensed under Wildace Software License Agreement ("License").
 * You may not use this file except in compliance with the License.
 *
 * NOTICE
 * ALL INFORMATION CONTAINED HEREIN IS, AND REMAINS THE PROPERTY OF WILDACE PRIVATE LIMITED.
 * THE INTELLECTUAL AND TECHNICAL CONCEPTS CONTAINED HEREIN ARE PROPRIETARY TO WILDACE PRIVATE LIMITED AND ARE PROTECTED BY TRADE SECRET OR COPYRIGHT LAW.
 * DISSEMINATION OF THIS INFORMATION OR REPRODUCTION OF THIS MATERIAL IS STRICTLY FORBIDDEN UNLESS PRIOR WRITTEN PERMISSION IS OBTAINED FROM WILDACE PRIVATE LIMITED.
 * **********************************************************************************************************************************************************************
 * Change History
 * **********************************************************************************************************************************************************************
 * |     Date      |     Name     |      Change     |      Details
 * |  15/05/2025   | Wilson Sam   |     Created     |  File Creation
 * **********************************************************************************************************************************************************************
 * */
package com.wildace.roulette.domain.api;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for performing a roulette spin")
public class SpinRequest {

    @Schema(description = "Electronic Gaming Machine (EGM) identifier", example = "WAS-1001", required = true)
    private String egmId;

    @Schema(description = "User unique identifier (UID)", example = "53ce81df", required = true)
    private String uid;

    @Schema(description = "List of bets placed for this spin", required = true)
    private List<Bet> betsList;

    public String getEgmId() {
        return egmId;
    }

    public void setEgmId(String egmId) {
        this.egmId = egmId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Bet> getBetsList() {
        return betsList;
    }

    public void setBetsList(List<Bet> betsList) {
        this.betsList = betsList;
    }

    @Schema(description = "Single bet placed by the user")
    public static class Bet {
        @Schema(description = "Index of the bet", example = "900", required = true)
        private int betIndex;

        @Schema(description = "Amount placed for this bet", example = "100.0", required = true)
        private double betAmount;

        public int getBetIndex() {
            return betIndex;
        }

        public void setBetIndex(int betIndex) {
            this.betIndex = betIndex;
        }

        public double getBetAmount() {
            return betAmount;
        }

        public void setBetAmount(double betAmount) {
            this.betAmount = betAmount;
        }
    }
}