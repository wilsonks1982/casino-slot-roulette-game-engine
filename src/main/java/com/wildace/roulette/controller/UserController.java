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
package com.wildace.roulette.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wildace.roulette.domain.api.UserDepositRequest;
import com.wildace.roulette.domain.documents.Transaction;
import com.wildace.roulette.domain.documents.User;
import com.wildace.roulette.services.TransactionService;
import com.wildace.roulette.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/api/v1")
public class UserController {

	private final UserService userService;
	private final TransactionService transactionService;

	@Autowired
	public UserController(UserService userService, TransactionService transactionService) {
		this.userService = userService;
		this.transactionService = transactionService;
	}

    public static String generatePrimaryKey() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return now.format(formatter);
    }
    
	@Operation(summary = "Get all users", description = "Retrieves a list of all users in the system.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
	})
	@GetMapping(path = "/users")
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	
	@Operation(summary = "Get user by UID", description = "Retrieves a user based on their unique identifier (UID).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User found",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/users/{uid}")
	public User getUserByUid(@Parameter(description = "Unique identifier of the user", required = true) @PathVariable String uid) {
		return userService.findOne(uid);
	}

	// Get User by UID passed as a query parameter - For Backward compatibility
	@Operation(summary = "Get user by UID (Query Parameter)", description = "Retrieves a user based on their UID passed as a query parameter (backward compatibility).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User found",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/user")
	public User getUserByUidQuery(@Parameter(description = "Unique identifier of the user", required = true) @RequestParam String uid) {
		return userService.findOne(uid);
	}

	

	@Operation(summary = "Deposit credit for user", description = "Deposits credit into a user's wallet and creates a deposit transaction.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Deposit successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	@PostMapping("/deposit")
	public User updateUserByUidDeposit(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Deposit request data", required = true,
			content = @Content(schema = @Schema(implementation = UserDepositRequest.class))) @RequestBody UserDepositRequest request) {
		String uid = request.getUid();
		String egmId = request.getEgmId();
		int credit = request.getCredit();
		String transBy = request.getTransBy();
		
		// Create a new transaction
		Transaction transaction = new Transaction();
		transaction.setEgmId(egmId);
		transaction.setUid(uid);
		transaction.setTransId(generatePrimaryKey());
		transaction.setTransType("Deposit");
		transaction.setTransBy(transBy); // Assuming transBy is the user who made the transaction		
		transaction.setDepositAmount((double) credit);
		transaction.setWithdrawAmount(0.0);
		transaction.setPrevCredit((double) credit);
		transaction.setThenCredit(0.0);
		transaction.setTransStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		
		
		// Save the transaction to the database
		transactionService.createTransaction(transaction);

		User user = userService.findOne(uid);
		user.setWallet(user.getWallet() + credit);
		user.setIsPlaying(true);
		user.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		return userService.updateOne(uid, user);
	}

	@Operation(summary = "Withdraw credit for user", description = "Withdraws credit from a user's wallet and creates a withdrawal transaction.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Withdrawal successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	@PostMapping("/withdraw")
	public User updateUserByUidWithdraw(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Withdraw request data", required = true,
					content = @Content(schema = @Schema(implementation = UserDepositRequest.class)))
			@RequestBody UserDepositRequest request) {
		String uid = request.getUid();
		String egmId = request.getEgmId();
		int credit = request.getCredit();
		String transBy = request.getTransBy();
		
		// Create a new transaction
		Transaction transaction = new Transaction();
		transaction.setEgmId(egmId);
		transaction.setUid(uid);
		transaction.setTransId(generatePrimaryKey());
		transaction.setTransType("Withdraw");
		transaction.setTransBy(transBy); // Assuming transBy is the user who made the transaction		
		transaction.setDepositAmount(0.0);
		transaction.setWithdrawAmount((double) credit);
		transaction.setPrevCredit(0.0);
		transaction.setThenCredit((double) credit);
		transaction.setTransStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				
		// Save the transaction to the database
		transactionService.createTransaction(transaction);

		User user = userService.findOne(uid);
		user.setWallet(user.getWallet() - credit);
		user.setIsPlaying(false);
		user.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		return userService.updateOne(uid, user);
	}

	
	@Operation(summary = "Create a new user", description = "Creates a new user with the provided details.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User created successfully",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	@PostMapping("/users")
	public User createUser(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "User object to create", required = true,
			content = @Content(schema = @Schema(implementation = User.class)))
		@RequestBody User user) {
		return userService.createUser(user);
	}

	// Endpoint to delete a user by UID
	@Operation(summary = "Delete a user", description = "Deletes a user based on their unique identifier (UID).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User deleted successfully"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	@DeleteMapping("/users/{uid}")
	public void deleteUserByUid(@Parameter(description = "Unique identifier of the user to delete", required = true) @PathVariable String uid) {
		userService.deleteUserByUid(uid);
	}
}
