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
package com.wildace.roulette.domain.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import io.swagger.v3.oas.annotations.media.Schema;

@Document(collection = "transactions")
@Schema(description = "Transaction object representing a deposit or withdrawal operation")
public class Transaction {

    @Id
    @Schema(description = "MongoDB internal transaction ID (ObjectId)", example = "6654fdc7a5b6e56c2c3b7a1b", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("egmId")
    @Schema(description = "Egm (Electronic Gaming Machine) identifier", example = "EGM001", required = true)
    private String egmId;

    @Field("uid")
    @Schema(description = "User UID associated with the transaction", example = "U12345", required = true)
    private String uid;

    @Field("transId")
    @Schema(description = "Unique transaction ID (business primary key)", example = "20250603132051123", required = true)
    private String transId;

    @Field("transType")
    @Schema(description = "Type of transaction (Deposit/Withdraw)", example = "Deposit", required = true)
    private String transType;

    @Field("transBy")
    @Schema(description = "UID of the user who performed the transaction", example = "Uadmin", required = true)
    private String transBy;

    @Field("depositAmount")
    @Schema(description = "Deposit amount in the transaction", example = "100.0")
    private Double depositAmount;

    @Field("withdrawAmount")
    @Schema(description = "Withdraw amount in the transaction", example = "0.0")
    private Double withdrawAmount;

    @Field("prevCredit")
    @Schema(description = "Credit before transaction", example = "0.0")
    private Double prevCredit;

    @Field("thenCredit")
    @Schema(description = "Credit after transaction", example = "100.0")
    private Double thenCredit;

    @Field("transStartTime")
    @Schema(description = "Transaction start time (yyyy-MM-dd HH:mm:ss)", example = "2025-06-03 13:20:51")
    private String transStartTime;

    @Field("transEndTime")
    @Schema(description = "Transaction end time (yyyy-MM-dd HH:mm:ss)", example = "2025-06-03 13:21:15")
    private String transEndTime;

    // Getters and Setters for all fields
    public String getId() {
        return id;
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

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTransBy() {
        return transBy;
    }

    public void setTransBy(String transBy) {
        this.transBy = transBy;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Double getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(Double withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }

    public Double getPrevCredit() {
        return prevCredit;
    }

    public void setPrevCredit(Double prevCredit) {
        this.prevCredit = prevCredit;
    }

    public Double getThenCredit() {
        return thenCredit;
    }

    public void setThenCredit(Double thenCredit) {
        this.thenCredit = thenCredit;
    }

    public String getTransStartTime() {
        return transStartTime;
    }

    public void setTransStartTime(String transStartTime) {
        this.transStartTime = transStartTime;
    }

    public String getTransEndTime() {
        return transEndTime;
    }

    public void setTransEndTime(String transEndTime) {
        this.transEndTime = transEndTime;
    }
}