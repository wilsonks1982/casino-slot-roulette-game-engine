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

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;

@Document(collection = "users")
@Schema(description = "User object representing an individual user in the system")
public class User {

	@Id
	@Schema(description = "MongoDB internal ID (ObjectId)", example = "6654fdc7a5b6e56c2c3b7a1a", accessMode = Schema.AccessMode.READ_ONLY)
	private String _id;

	@Schema(description = "Internal numeric ID", example = "1001", accessMode = Schema.AccessMode.READ_ONLY)
	private Integer id;

	@Schema(description = "Unique user identifier (UID)", example = "U12345", required = true)
	private String uid;

	@Schema(description = "Role of the user", example = "PLAYER")
	private String role;

	@Schema(description = "User nickname", example = "aceplayer")
	private String nickname;

	@Schema(description = "First name of the user", example = "John")
	private String firstName;

	@Schema(description = "Last name of the user", example = "Doe")
	private String lastName;

	@Schema(description = "Current wallet balance", example = "2500.50")
	private Double wallet;

	@Schema(description = "4-digit PIN for user authentication", example = "1234")
	private Integer pin;

	@Schema(description = "Whether the user is currently playing", example = "true")
	private Boolean isPlaying;

	@Schema(description = "Date and time when the user was created (yyyy-MM-dd HH:mm:ss)", example = "2025-05-15 14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
	private String createdAt;

	@Schema(description = "Date and time when the user was last updated (yyyy-MM-dd HH:mm:ss)", example = "2025-06-03 13:18:08", accessMode = Schema.AccessMode.READ_ONLY)
	private String updatedAt;

	// Constructor
	public User() {
	}

	// Getters and Setters
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Double getWallet() {
		return wallet;
	}

	public void setWallet(Double wallet) {
		this.wallet = wallet;
	}

	public Integer getPin() {
		return pin;
	}

	public void setPin(Integer pin) {
		this.pin = pin;
	}

	public Boolean getIsPlaying() {
		return isPlaying;
	}

	public void setIsPlaying(Boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "User{" + "uid='" + uid + '\'' + ", id=" + id + ", role='" + role + '\'' + ", nickname='" + nickname
				+ '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", wallet=" + wallet
				+ ", pin=" + pin + ", isPlaying=" + isPlaying + ", createdAt='" + createdAt + '\'' + ", updatedAt='"
				+ updatedAt + '\'' + '}';
	}
}