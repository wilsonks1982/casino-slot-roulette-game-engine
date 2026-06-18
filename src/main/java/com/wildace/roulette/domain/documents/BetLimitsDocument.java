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

@Document(collection = "limits")
@Schema(description = "MongoDB document representing bet limits for categories")
public class BetLimitsDocument {

    @Id
    @Schema(description = "Unique identifier for the document", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("limits")
    @Schema(description = "List of categories with their respective limits")
    private List<Category> limits;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Category> getLimits() {
        return limits;
    }

    public void setLimits(List<Category> limits) {
        this.limits = limits;
    }

    @Schema(description = "Category containing a name and associated limits")
    public static class Category {
        @Schema(description = "Category name", example = "General Bet Limits")
        private String category;

        @Schema(description = "List of limits for this category")
        private List<Limit> limits;

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public List<Limit> getLimits() {
            return limits;
        }

        public void setLimits(List<Limit> limits) {
            this.limits = limits;
        }
    }

    @Schema(description = "Single limit entry with name and amount")
    public static class Limit {
        @Schema(description = "Limit ID", example = "1")
        private int id;

        @Schema(description = "Name of the limit", example = "Min Straight Up Bet")
        private String name;

        @Schema(description = "Limit amount", example = "5000")
        private int amount;

        // Getters and Setters
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

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}