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

@Schema(description = "Response object for a completed roulette spin")
public class SpinResponse {

    @Schema(description = "Unique ID of the spin record", example = "20250603114301037")
    private String id;

    @Schema(description = "Electronic Gaming Machine (EGM) identifier", example = "WAS-1001")
    private String egmId;

    @Schema(description = "User unique identifier (UID)", example = "53ce81df")
    private String uid;

    @Schema(description = "User's wallet balance after spin", example = "2000.0")
    private double wallet;

    @Schema(description = "Spin number for this EGM", example = "000021")
    private String spinNumber;

    @Schema(description = "List of bets placed for this spin")
    private List<Bet> betsList;

    @Schema(description = "List of bets that won in this spin")
    private List<WonBet> wonBetsList;

    @Schema(description = "Total amount bet in this spin", example = "500.0")
    private double betAmount;

    @Schema(description = "Total amount won in this spin", example = "1000.0")
    private double winAmount;

    @Schema(description = "User's credit before the spin", example = "2500.0")
    private double oldCredit;

    @Schema(description = "User's credit after the spin", example = "3000.0")
    private double newCredit;

    @Schema(description = "Result number of the spin", example = "7")
    private int number;

    @Schema(description = "Spin start timestamp (yyyy-MM-dd HH:mm:ss)", example = "2025-06-03 13:30:00")
    private String spinStart;

    @Schema(description = "Spin end timestamp (yyyy-MM-dd HH:mm:ss)", example = "2025-06-03 13:30:05")
    private String spinEnd;

    @Schema(description = "Operation status: 1 for success, 0 for failure", example = "1")
    private int ok;

    @Schema(description = "Message describing the result", example = "Spin completed successfully")
    private String msg;

    @Schema(description = "HTTP-like status code for the response", example = "200")
    private int statusCode;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public String getSpinNumber() {
        return spinNumber;
    }

    public void setSpinNumber(String spinNumber) {
        this.spinNumber = spinNumber;
    }

    public List<Bet> getBetsList() {
        return betsList;
    }

    public void setBetsList(List<Bet> betsList) {
        this.betsList = betsList;
    }

    public List<WonBet> getWonBetsList() {
        return wonBetsList;
    }

    public void setWonBetsList(List<WonBet> wonBetsList) {
        this.wonBetsList = wonBetsList;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public double getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(double winAmount) {
        this.winAmount = winAmount;
    }

    public double getOldCredit() {
        return oldCredit;
    }

    public void setOldCredit(double oldCredit) {
        this.oldCredit = oldCredit;
    }

    public double getNewCredit() {
        return newCredit;
    }

    public void setNewCredit(double newCredit) {
        this.newCredit = newCredit;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSpinStart() {
        return spinStart;
    }

    public void setSpinStart(String spinStart) {
        this.spinStart = spinStart;
    }

    public String getSpinEnd() {
        return spinEnd;
    }

    public void setSpinEnd(String spinEnd) {
        this.spinEnd = spinEnd;
    }

    @Schema(description = "Bet placed by the user in the spin")
    public static class Bet {
        @Schema(description = "Index of the bet", example = "2")
        private int betIndex;

        @Schema(description = "Amount placed for this bet", example = "100.0")
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

    @Schema(description = "Details of a bet that resulted in a win")
    public static class WonBet {
        @Schema(description = "Index of the bet", example = "2")
        private int betIndex;

        @Schema(description = "Amount placed for this bet", example = "100.0")
        private double betAmount;

        @Schema(description = "Win amount for this bet", example = "350.0")
        private double winAmount;

        @Schema(description = "Name of the bet (e.g., color, number)", example = "Red")
        private String name;

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

        public double getWinAmount() {
            return winAmount;
        }

        public void setWinAmount(double winAmount) {
            this.winAmount = winAmount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}