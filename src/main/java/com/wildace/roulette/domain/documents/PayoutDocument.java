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
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Document(collection = "payouts")
@Schema(description = "MongoDB document representing payout details for bet types")
public class PayoutDocument {

    @Id
    @Schema(description = "Unique identifier for the document", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("payouts")
    @Schema(description = "List of payouts for different bet types")
    private List<Payout> payouts;

    public PayoutDocument() {}

    public PayoutDocument(String id, List<Payout> payouts) {
        this.id = id;
        this.payouts = payouts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Payout> getPayouts() {
        return payouts;
    }

    public void setPayouts(List<Payout> payouts) {
        this.payouts = payouts;
    }

    @Schema(description = "Payout details for a specific bet type")
    public static class Payout {
        @Schema(description = "Payout ID", example = "1")
        private int id;

        @Schema(description = "Name of the bet type", example = "Straight Up")
        private String name;

        @Schema(description = "Payout ratio for the bet type", example = "35")
        private int payout;

        public Payout() {}

        public Payout(int id, String name, int payout) {
            this.id = id;
            this.name = name;
            this.payout = payout;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPayout() {
            return payout;
        }

        public void setPayout(int payout) {
            this.payout = payout;
        }
    }
}