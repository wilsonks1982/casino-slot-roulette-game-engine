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
package com.wildace.roulette.services;

import com.wildace.roulette.events.UserBalanceUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.wildace.roulette.domain.documents.User;
import com.wildace.roulette.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public UserService(UserRepository userRepository, ApplicationEventPublisher publisher) {
        this.userRepository = userRepository;
        this.publisher = publisher;
    }
    
    // Method to get all users
    public List<User> getAllUsers() {
		return userRepository.findAll();
	}

    // Method to find a user by UID
    public User findOne(String uid) {
        return userRepository.findOne(uid);
    }
    // Method to update a user by UID
    public User updateOne(String uid, User updatedUser) {
    	User existingUser = userRepository.findOne(uid);
		if (existingUser != null) {
			// Update the fields of the existing user with the updated user
			existingUser.setWallet(updatedUser.getWallet());
			existingUser.setUpdatedAt(updatedUser.getUpdatedAt());
			
			// Add other fields as necessary
			return userRepository.save(existingUser);
		}
		return null; // or throw an exception if user not found
    	
	}
    
    // Example method to create or save a user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Example method to delete a user by UID
    public void deleteUserByUid(String uid) {
        Optional<User> user = Optional.ofNullable(userRepository.findOne(uid));
        user.ifPresent(userRepository::delete);
    }


    /**
     * Debit wallet if sufficient funds exist.
     * @return true if debited; false if user not found or insufficient funds.
     */
    public boolean debitWallet(String uid, double amount, String roundId, String reason) {
        if (uid == null || uid.isBlank()) return false;
        if (amount <= 0) return true;

        User user = userRepository.findOne(uid);
        if (user == null) return false;

        double current = user.getWallet() != null ? user.getWallet() : 0.0;
        if (current < amount) return false;

        user.setWallet(current - amount);
        userRepository.save(user);

        publisher.publishEvent(new UserBalanceUpdatedEvent(
                uid,
                roundId,
                user.getWallet(),
                reason
        ));

        return true;
    }

    public boolean creditWallet(String uid, double amount, String roundId, String reason) {
        if (uid == null || uid.isBlank()) return false;
        if (amount <= 0) return true;

        User user = userRepository.findOne(uid);
        if (user == null) return false;

        double current = user.getWallet() != null ? user.getWallet() : 0.0;
        user.setWallet(current + amount);
        userRepository.save(user);

        publisher.publishEvent(new UserBalanceUpdatedEvent(
                uid,
                roundId,
                user.getWallet(),
                reason
        ));

        return true;
    }

//    // keep your old signatures for compatibility (delegate)
//    public boolean debitWallet(String uid, double amount) {
//        return debitWallet(uid, amount, null, "Wallet debited: " + amount);
//    }
//
//    public boolean creditWallet(String uid, double amount) {
//        return creditWallet(uid, amount, null, "Wallet credited: " + amount);
//    }
}
