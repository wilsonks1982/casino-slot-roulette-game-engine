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

@Document(collection = "coins")
@Schema(description = "MongoDB document representing available coin denominations and their properties")
public class CoinDocument {

    @Id
    @Schema(description = "Unique identifier for the document", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("coins")
    @Schema(description = "List of coins with their denomination, color, and currency")
    private List<Coin> coins;

    public CoinDocument() {}

    public CoinDocument(String id, List<Coin> coins) {
        this.id = id;
        this.coins = coins;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public void setCoins(List<Coin> coins) {
        this.coins = coins;
    }

    @Schema(description = "Coin denomination and its attributes")
    public static class Coin {
        @Schema(description = "Coin ID", example = "1")
        private int id;

        @Schema(description = "Denomination value of the coin", example = "100")
        private int denom;

        @Schema(description = "Color of the coin", example = "red")
        private String color;

        @Schema(description = "Currency of the coin", example = "INR")
        private String currency;

        public Coin() {
        }

        public Coin(int id, int denom, String color, String currency) {
            this.id = id;
            this.denom = denom;
            this.color = color;
            this.currency = currency;
        }

        // Getters and Setters

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDenom() {
            return denom;
        }

        public void setDenom(int denom) {
            this.denom = denom;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}