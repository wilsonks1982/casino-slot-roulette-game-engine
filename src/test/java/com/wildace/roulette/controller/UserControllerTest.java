package com.wildace.roulette.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Test
    void generatePrimaryKey() {
        String primaryKey = UserController.generatePrimaryKey();
        assertNotNull(primaryKey);
        assertEquals(17, primaryKey.length()); // yyyyMMddHHmmssSSS = 17 characters
    }

    @Test
    void generatePrimaryKey_isNumeric() {
        String primaryKey = UserController.generatePrimaryKey();
        assertTrue(primaryKey.matches("\\d{17}"), "Primary key should contain only digits");
    }

    @Test
    void generatePrimaryKey_isUnique() {
        String key1 = UserController.generatePrimaryKey();
        //Mimic 1 sec delay to ensure different timestamp
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String key2 = UserController.generatePrimaryKey();
        assertNotEquals(key1, key2, "Consecutive calls should generate different keys");
    }

    @Test
    void generatePrimaryKey_startsWithCurrentDate() {
        String primaryKey = UserController.generatePrimaryKey();
        String yearPrefix = String.valueOf(java.time.Year.now().getValue());
        assertTrue(primaryKey.startsWith(yearPrefix), "Primary key should start with current year");
    }

}