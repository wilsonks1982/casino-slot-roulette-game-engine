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
 * |  01/07/2025   | Wilson Sam   |     Updated     |  Spin Request now supports empty bets
 * **********************************************************************************************************************************************************************
 * */
package com.wildace.roulette.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wildace.roulette.domain.Stat;
import com.wildace.roulette.domain.api.SpinRequest;
import com.wildace.roulette.domain.api.SpinResponse;
import com.wildace.roulette.domain.documents.BetLimitsDocument;
import com.wildace.roulette.domain.documents.CoinDocument;
import com.wildace.roulette.domain.documents.PayoutDocument;
import com.wildace.roulette.domain.documents.SpinResult;
import com.wildace.roulette.services.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Tag(name = "Roulette API v2", description = "Endpoints for managing roulette game, spins, coins, limits, and payouts")
@RestController
@RequestMapping("/api/v2/roulette")
public class RouletteAPI2Controller {

    private final RouletteService rouletteService;
    private final CoinService coinService;
    private final PayoutService payoutService;
    private final BetLimitsService betLimitsService;
    private final SpinResultService spinResultService;
    private final UserService userService;
    private final TransactionService transactionService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RouletteAPI2Controller.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int STATUS_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_SERVER_ERROR = 500;

    public RouletteAPI2Controller(
            RouletteService rouletteService,
            CoinService coinService,
            PayoutService payoutService,
            BetLimitsService betLimitsService,
            SpinResultService spinResultService,
            UserService userService,
            TransactionService transactionService) {
        this.rouletteService = rouletteService;
        this.coinService = coinService;
        this.payoutService = payoutService;
        this.betLimitsService = betLimitsService;
        this.spinResultService = spinResultService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    // ---------- Stats & Hot/Cold Numbers ----------

    @Operation(summary = "Get top 5 hot numbers", description = "Returns the 5 most frequently drawn numbers.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/hot-numbers")
    public Map<Integer, Long> sendHotNumbersJson() {
        var spinResults = spinResultService.getAllSpinResults();
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
        return winNumberFrequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(6)// Limit to 6
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    @Operation(summary = "Get top 5 hot numbers for a user", description = "Returns the 5 most frequently drawn numbers for a user.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/hot-numbers/uid/{uid}")
    public Map<Integer, Long> sendHotNumbersJson(@Parameter(description = "UID", required = true) @PathVariable String uid) {

    	Optional<List<SpinResult>> spinResult = spinResultService.getSpinResultByUid(uid);
    	if (spinResult.isEmpty() || spinResult.get().isEmpty()) {
			return Map.of(); // Return empty map if no results found
		}
        
    	Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResult.get());
    	return winNumberFrequency.entrySet().stream()
    			.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
    			.limit(6)// Limit to 6
    			.collect(Collectors.toMap(
    					Map.Entry::getKey,
    					Map.Entry::getValue,
    					(e1, e2) -> e1,
    					LinkedHashMap::new
    					));
    }

    @Operation(summary = "Get top 5 hot numbers for EGM", description = "Returns the 5 most frequently drawn numbers for an EGM.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/hot-numbers/egm/{egmId}")
    public Map<Integer, Long> sendHotNumbersJsonByEgmId(@Parameter(description = "EGM ID", required = true) @PathVariable String egmId) {
		Optional<List<SpinResult>> spinResults = spinResultService.getSpinResultByEgmId(egmId);
		if (spinResults.isEmpty() || spinResults.get().isEmpty()) {
			return Map.of(); // Return empty map if no results found
		}
		Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults.get());
		return winNumberFrequency.entrySet().stream()
				.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
				.limit(6)// Limit to 6
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}
    
    @Operation(summary = "Get top 5 cold numbers", description = "Returns the 5 least frequently drawn numbers.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/cold-numbers")
    public Map<Integer, Long> sendColdNumbersJson() {
        var spinResults = spinResultService.getAllSpinResults();
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
        return winNumberFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(6)// Limit to 6 
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Operation(summary = "Get top 5 cold numbers for a user", description = "Returns the 5 least frequently drawn numbers for a user.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/cold-numbers/uid/{uid}")
    public Map<Integer, Long> sendColdNumbersJson(@Parameter(description = "UID", required = true) @PathVariable String uid) {
		Optional<List<SpinResult>> spinResult = spinResultService.getSpinResultByUid(uid);
		if (spinResult.isEmpty() || spinResult.get().isEmpty()) {
			return Map.of(); // Return empty map if no results found
		}
		Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResult.get());
		return winNumberFrequency.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.limit(6)// Limit to 6
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}
    @Operation(summary = "Get top 5 cold numbers for EGM", description = "Returns the 5 least frequently drawn numbers for an EGM.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/cold-numbers/egm/{egmId}")
    public Map<Integer, Long> sendColdNumbersJsonByEgmId(@Parameter(description = "EGM ID", required = true) @PathVariable String egmId) {
    	
    	Optional<List<SpinResult>> spinResults = spinResultService.getSpinResultByEgmId(egmId);
    	if (spinResults.isEmpty() || spinResults.get().isEmpty()) {
    		return Map.of(); // Return empty map if no results found
    	}
    	
    	Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults.get());
		return winNumberFrequency.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.limit(6)// Limit to 6
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
		
    }
    
    
    
    @Operation(summary = "Get statistics for all numbers", description = "Returns statistics (frequency and percent) for each roulette number.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Stat.class))))
    @GetMapping("/statistics")
    public Map<Integer, Stat> sendStatisticsJson() {
        var spinResults = spinResultService.getAllSpinResults();
        if (spinResults == null || spinResults.isEmpty()) {
            var emptyStatistics = new LinkedHashMap<Integer, Stat>();
            for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++) {
                emptyStatistics.put(i, new Stat(0, 0.0));
            }
            return emptyStatistics;
        }
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
        Map<Integer, Long> winNumberFrequencyPercent = groupByWinNumberFrequencyPercent(spinResults);
        return winNumberFrequencyPercent.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new Stat(winNumberFrequency.getOrDefault(entry.getKey(), 0L), entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    @Operation(summary = "Get statistics for all numbers for a user", description = "Returns statistics (frequency and percent) for each roulette number for a user.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Stat.class))))
    @GetMapping("/statistics/uid/{uid}")
    public Map<Integer, Stat> sendStatisticsJson(@Parameter(description = "UID", required = true) @PathVariable String uid) {
		Optional<List<SpinResult>> spinResultsOpt = spinResultService.getSpinResultByUid(uid);
		if (spinResultsOpt.isEmpty() || spinResultsOpt.get().isEmpty()) {
			var emptyStatistics = new LinkedHashMap<Integer, Stat>();
			for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++) {
				emptyStatistics.put(i, new Stat(0, 0.0));
			}
			return emptyStatistics;
		}
		List<SpinResult> spinResults = spinResultsOpt.get();
		Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
		Map<Integer, Long> winNumberFrequencyPercent = groupByWinNumberFrequencyPercent(spinResults);
		return winNumberFrequencyPercent.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> new Stat(winNumberFrequency.getOrDefault(entry.getKey(), 0L), entry.getValue()),
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}
    
    @Operation(summary = "Get statistics for all numbers for EGM", description = "Returns statistics (frequency and percent) for each roulette number for an EGM.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Stat.class))))
    @GetMapping("/statistics/egm/{egmId}")
    public Map<Integer, Stat> sendStatisticsJsonByEgmId(@Parameter(description = "EGM ID", required = true) @PathVariable String egmId) {
    	Optional<List<SpinResult>> spinResultsOpt = spinResultService.getSpinResultByEgmId(egmId);
    	if (spinResultsOpt.isEmpty() || spinResultsOpt.get().isEmpty()) {    		
			var emptyStatistics = new LinkedHashMap<Integer, Stat>();
			for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++) {
				emptyStatistics.put(i, new Stat(0, 0.0));
			}
			return emptyStatistics;
		}
    	List<SpinResult> spinResults = spinResultsOpt.get();
    	Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
		Map<Integer, Long> winNumberFrequencyPercent = groupByWinNumberFrequencyPercent(spinResults);
		return winNumberFrequencyPercent.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> new Stat(winNumberFrequency.getOrDefault(entry.getKey(), 0L), entry.getValue()),
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
    }

    @Operation(summary = "Get group statistics", description = "Returns win percentages for groups: Red, Black, Odd, Even, 1-18, 19-36, Dozens, Columns.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/groups")
    public Map<String, Double> sendGroupsJson() {
        var spinResults = spinResultService.getAllSpinResults();
        if (spinResults.isEmpty()) {
            Map<String, Double> emptyGroupPercentages = new LinkedHashMap<>();
            emptyGroupPercentages.put("GroupRed", 0.0);
            emptyGroupPercentages.put("GroupBlack", 0.0);
            emptyGroupPercentages.put("GroupOdd", 0.0);
            emptyGroupPercentages.put("GroupEven", 0.0);
            emptyGroupPercentages.put("Group1to18", 0.0);
            emptyGroupPercentages.put("Group19to36", 0.0);
            emptyGroupPercentages.put("GroupDozen1", 0.0);
            emptyGroupPercentages.put("GroupDozen2", 0.0);
            emptyGroupPercentages.put("GroupDozen3", 0.0);
            emptyGroupPercentages.put("GroupColumn1", 0.0);
            emptyGroupPercentages.put("GroupColumn2", 0.0);
            emptyGroupPercentages.put("GroupColumn3", 0.0);
            return emptyGroupPercentages;
        }
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
        Map<String, Double> groupPercentages = new LinkedHashMap<>();
        groupPercentages.put("GroupRed", calculatePercentage(winNumberFrequency, RouletteGameLogics.RED, spinResults.size()));
        groupPercentages.put("GroupBlack", calculatePercentage(winNumberFrequency, RouletteGameLogics.BLACK, spinResults.size()));
        groupPercentages.put("GroupOdd", calculatePercentage(winNumberFrequency, RouletteGameLogics.ODD, spinResults.size()));
        groupPercentages.put("GroupEven", calculatePercentage(winNumberFrequency, RouletteGameLogics.EVEN, spinResults.size()));
        groupPercentages.put("Group1to18", calculatePercentage(winNumberFrequency, RouletteGameLogics.LOW, spinResults.size()));
        groupPercentages.put("Group19to36", calculatePercentage(winNumberFrequency, RouletteGameLogics.HIGH, spinResults.size()));
        groupPercentages.put("GroupDozen1", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN1, spinResults.size()));
        groupPercentages.put("GroupDozen2", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN2, spinResults.size()));
        groupPercentages.put("GroupDozen3", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN3, spinResults.size()));
        groupPercentages.put("GroupColumn1", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN1, spinResults.size()));
        groupPercentages.put("GroupColumn2", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN2, spinResults.size()));
        groupPercentages.put("GroupColumn3", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN3, spinResults.size()));
        return groupPercentages;
    }

    @Operation(summary = "Get group statistics for a user", description = "Returns win percentages for groups for a user.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/groups/uid/{uid}")
    public Map<String, Double> sendGroupsJson(@Parameter(description = "UID", required = true) @PathVariable String uid) {
    	Optional<List<SpinResult>> spinResultsOpt = spinResultService.getSpinResultByUid(uid);
    	if (spinResultsOpt.isEmpty() || spinResultsOpt.get().isEmpty()) {    		
			Map<String, Double> emptyGroupPercentages = new LinkedHashMap<>();
			emptyGroupPercentages.put("GroupRed", 0.0);
			emptyGroupPercentages.put("GroupBlack", 0.0);
			emptyGroupPercentages.put("GroupOdd", 0.0);
			emptyGroupPercentages.put("GroupEven", 0.0);
			emptyGroupPercentages.put("Group1to18", 0.0);
			emptyGroupPercentages.put("Group19to36", 0.0);
			emptyGroupPercentages.put("GroupDozen1", 0.0);
			emptyGroupPercentages.put("GroupDozen2", 0.0);
			emptyGroupPercentages.put("GroupDozen3", 0.0);
			emptyGroupPercentages.put("GroupColumn1", 0.0);
			emptyGroupPercentages.put("GroupColumn2", 0.0);
			emptyGroupPercentages.put("GroupColumn3", 0.0);
			return emptyGroupPercentages;
		}
    	List<SpinResult> spinResults = spinResultsOpt.get();
		Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
		Map<String, Double> groupPercentages = new LinkedHashMap<>();
		groupPercentages.put("GroupRed", calculatePercentage(winNumberFrequency, RouletteGameLogics.RED, spinResults.size()));
		groupPercentages.put("GroupBlack", calculatePercentage(winNumberFrequency, RouletteGameLogics.BLACK, spinResults.size()));
		groupPercentages.put("GroupOdd", calculatePercentage(winNumberFrequency, RouletteGameLogics.ODD, spinResults.size()));
		groupPercentages.put("GroupEven", calculatePercentage(winNumberFrequency, RouletteGameLogics.EVEN, spinResults.size()));
		groupPercentages.put("Group1to18", calculatePercentage(winNumberFrequency, RouletteGameLogics.LOW, spinResults.size()));
		groupPercentages.put("Group19to36", calculatePercentage(winNumberFrequency, RouletteGameLogics.HIGH, spinResults.size()));
		groupPercentages.put("GroupDozen1", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN1, spinResults.size()));
		groupPercentages.put("GroupDozen2", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN2, spinResults.size()));
		groupPercentages.put("GroupDozen3", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN3, spinResults.size()));
		groupPercentages.put("GroupColumn1", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN1, spinResults.size()));
		groupPercentages.put("GroupColumn2", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN2, spinResults.size()));
		groupPercentages.put("GroupColumn3", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN3, spinResults.size()));
		return groupPercentages;
    }
    @Operation(summary = "Get group statistics for EGM", description = "Returns win percentages for groups for an EGM.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")))
    @GetMapping("/groups/egm/{egmId}")
    public Map<String, Double> sendGroupsJsonByEgmId(@Parameter(description = "EGM ID", required = true) @PathVariable String egmId) {
		Optional<List<SpinResult>> spinResultsOpt = spinResultService.getSpinResultByEgmId(egmId);
		if (spinResultsOpt.isEmpty() || spinResultsOpt.get().isEmpty()) {
			Map<String, Double> emptyGroupPercentages = new LinkedHashMap<>();
			emptyGroupPercentages.put("GroupRed", 0.0);
			emptyGroupPercentages.put("GroupBlack", 0.0);
			emptyGroupPercentages.put("GroupOdd", 0.0);
			emptyGroupPercentages.put("GroupEven", 0.0);
			emptyGroupPercentages.put("Group1to18", 0.0);
			emptyGroupPercentages.put("Group19to36", 0.0);
			emptyGroupPercentages.put("GroupDozen1", 0.0);
			emptyGroupPercentages.put("GroupDozen2", 0.0);
			emptyGroupPercentages.put("GroupDozen3", 0.0);
			emptyGroupPercentages.put("GroupColumn1", 0.0);
			emptyGroupPercentages.put("GroupColumn2", 0.0);
			emptyGroupPercentages.put("GroupColumn3", 0.0);
			return emptyGroupPercentages;
		}
		List<SpinResult> spinResults = spinResultsOpt.get();
		Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(spinResults);
		Map<String, Double> groupPercentages = new LinkedHashMap<>();
		groupPercentages.put("GroupRed", calculatePercentage(winNumberFrequency, RouletteGameLogics.RED, spinResults.size()));
		groupPercentages.put("GroupBlack", calculatePercentage(winNumberFrequency, RouletteGameLogics.BLACK, spinResults.size()));
		groupPercentages.put("GroupOdd", calculatePercentage(winNumberFrequency, RouletteGameLogics.ODD, spinResults.size()));
		groupPercentages.put("GroupEven", calculatePercentage(winNumberFrequency, RouletteGameLogics.EVEN, spinResults.size()));
		groupPercentages.put("Group1to18", calculatePercentage(winNumberFrequency, RouletteGameLogics.LOW, spinResults.size()));
		groupPercentages.put("Group19to36", calculatePercentage(winNumberFrequency, RouletteGameLogics.HIGH, spinResults.size()));
		groupPercentages.put("GroupDozen1", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN1, spinResults.size()));
		groupPercentages.put("GroupDozen2", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN2, spinResults.size()));
		groupPercentages.put("GroupDozen3", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN3, spinResults.size()));
		groupPercentages.put("GroupColumn1", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN1, spinResults.size()));
		groupPercentages.put("GroupColumn2", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN2, spinResults.size()));
		groupPercentages.put("GroupColumn3", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN3, spinResults.size()));
		return groupPercentages;
    }
    
    // ---------- SpinResult API ----------

    @Operation(summary = "Create or update a SpinResult")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SpinResult.class))))
    @PostMapping("/results")
    public ResponseEntity<SpinResult> createOrUpdateSpinResult(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "SpinResult to create or update", required = true, content = @Content(schema = @Schema(implementation = SpinResult.class)))
            @RequestBody SpinResult spinResult) {
        SpinResult savedSpinResult = spinResultService.saveSpinResult(spinResult);
        return new ResponseEntity<>(savedSpinResult, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all SpinResults")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SpinResult.class)))))
    @GetMapping("/results")
    public ResponseEntity<List<SpinResult>> getAllSpinResults() {
        List<SpinResult> spinResults = spinResultService.getAllSpinResults();
        return new ResponseEntity<>(spinResults, HttpStatus.OK);
    }

    @Operation(summary = "Get a SpinResult by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SpinResult.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/results/{id}")
    public ResponseEntity<SpinResult> getSpinResultById(
            @Parameter(description = "SpinResult ID", required = true) @PathVariable Integer id) {
        Optional<SpinResult> spinResult = spinResultService.getSpinResultById(id);
        return spinResult.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get a SpinResult by UID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SpinResult.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    
    @GetMapping("/results/egm/{egmId}")
    public ResponseEntity<List<SpinResult>> getSpinResultByEgmId(
			@Parameter(description = "SpinResult EGM ID", required = true) @PathVariable String egmId) {
		Optional<List<SpinResult>> spinResults = spinResultService.getSpinResultByEgmId(egmId);
		if (spinResults.isPresent() && !spinResults.get().isEmpty()) {
			return new ResponseEntity<>(spinResults.get(), HttpStatus.OK);
		} else {
			//Send EMpty List if no results found
			return new ResponseEntity<>(List.of(), HttpStatus.OK);
		}
	}
    
    @GetMapping("/results/uid/{uid}")
    public ResponseEntity<List<SpinResult>> getSpinResultByUid(
            @Parameter(description = "SpinResult UID", required = true) @PathVariable String uid) {
    			Optional<List<SpinResult>> spinResults = spinResultService.getSpinResultByUid(uid);
    	if (spinResults.isPresent() && !spinResults.get().isEmpty()) {
			return new ResponseEntity<>(spinResults.get(), HttpStatus.OK);
		} else {
			//Send EMpty List if no results found
			return new ResponseEntity<>(List.of(), HttpStatus.OK);
		}
    }

    @Operation(summary = "Delete a SpinResult by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/results/{id}")
    public ResponseEntity<Void> deleteSpinResultById(
            @Parameter(description = "SpinResult ID", required = true) @PathVariable Integer id) {
        Optional<SpinResult> spinResult = spinResultService.getSpinResultById(id);
        if (spinResult.isPresent()) {
            spinResultService.deleteSpinResultById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Update a SpinResult by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = SpinResult.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/results/{id}")
    public ResponseEntity<SpinResult> updateSpinResult(
            @Parameter(description = "SpinResult ID", required = true) @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "SpinResult object", required = true, content = @Content(schema = @Schema(implementation = SpinResult.class)))
            @RequestBody SpinResult spinResult) {
        try {
            SpinResult updatedSpinResult = spinResultService.updateSpinResult(id, spinResult);
            return new ResponseEntity<>(updatedSpinResult, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ---------- BetLimits API ----------

    @Operation(summary = "Create or update BetLimits")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = BetLimitsDocument.class))))
    @PostMapping("/limits")
    public ResponseEntity<BetLimitsDocument> createOrUpdateBetLimits(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "BetLimitsDocument object", required = true, content = @Content(schema = @Schema(implementation = BetLimitsDocument.class)))
            @RequestBody BetLimitsDocument betLimitsDocument) {
        BetLimitsDocument savedDocument = betLimitsService.createBetLimits(betLimitsDocument);
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all BetLimits")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BetLimitsDocument.class)))))
    @GetMapping("/limits")
    public ResponseEntity<List<BetLimitsDocument>> getAllBetLimits() {
        List<BetLimitsDocument> betLimits = betLimitsService.getAllBetLimits();
        return new ResponseEntity<>(betLimits, HttpStatus.OK);
    }

    @Operation(summary = "Get BetLimits by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BetLimitsDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/limits/{id}")
    public ResponseEntity<BetLimitsDocument> getBetLimitsById(
            @Parameter(description = "BetLimitsDocument ID", required = true) @PathVariable String id) {
        Optional<BetLimitsDocument> betLimits = betLimitsService.getBetLimitsById(id);
        return betLimits.map(document -> new ResponseEntity<>(document, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Update BetLimits by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = BetLimitsDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/limits/{id}")
    public ResponseEntity<BetLimitsDocument> updateBetLimits(
            @Parameter(description = "BetLimitsDocument ID", required = true) @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated BetLimitsDocument", required = true, content = @Content(schema = @Schema(implementation = BetLimitsDocument.class)))
            @RequestBody BetLimitsDocument updatedBetLimits) {
        try {
            BetLimitsDocument savedDocument = betLimitsService.updateBetLimits(id, updatedBetLimits);
            return new ResponseEntity<>(savedDocument, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete BetLimits by ID")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Deleted"))
    @DeleteMapping("/limits/{id}")
    public ResponseEntity<Void> deleteBetLimits(
            @Parameter(description = "BetLimitsDocument ID", required = true) @PathVariable String id) {
        betLimitsService.deleteBetLimitsById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ---------- PayoutDocument API ----------

    @Operation(summary = "Create or update PayoutDocument")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = PayoutDocument.class))))
    @PostMapping("/payouts")
    public ResponseEntity<PayoutDocument> createOrUpdatePayout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PayoutDocument object", required = true, content = @Content(schema = @Schema(implementation = PayoutDocument.class)))
            @RequestBody PayoutDocument payoutDocument) {
        PayoutDocument savedPayout = payoutService.savePayout(payoutDocument);
        return new ResponseEntity<>(savedPayout, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all Payouts")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PayoutDocument.class)))))
    @GetMapping("/payouts")
    public ResponseEntity<List<PayoutDocument>> getAllPayouts() {
        List<PayoutDocument> payouts = payoutService.getAllPayouts();
        return new ResponseEntity<>(payouts, HttpStatus.OK);
    }

    @Operation(summary = "Get Payout by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PayoutDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/payouts/{id}")
    public ResponseEntity<PayoutDocument> getPayoutById(
            @Parameter(description = "PayoutDocument ID", required = true) @PathVariable String id) {
        Optional<PayoutDocument> payout = payoutService.getPayoutById(id);
        return payout.map(document -> new ResponseEntity<>(document, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Update Payout by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = PayoutDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/payouts/{id}")
    public ResponseEntity<PayoutDocument> updatePayout(
            @Parameter(description = "PayoutDocument ID", required = true) @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated PayoutDocument", required = true, content = @Content(schema = @Schema(implementation = PayoutDocument.class)))
            @RequestBody PayoutDocument updatedPayout) {
        try {
            PayoutDocument savedPayout = payoutService.updatePayout(id, updatedPayout);
            return new ResponseEntity<>(savedPayout, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete Payout by ID")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Deleted"))
    @DeleteMapping("/payouts/{id}")
    public ResponseEntity<Void> deletePayout(
            @Parameter(description = "PayoutDocument ID", required = true) @PathVariable String id) {
        payoutService.deletePayoutById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ---------- CoinDocument API ----------

    @Operation(summary = "Create a new CoinDocument")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CoinDocument.class))))
    @PostMapping("/coins")
    public ResponseEntity<CoinDocument> createCoinDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CoinDocument object", required = true, content = @Content(schema = @Schema(implementation = CoinDocument.class)))
            @RequestBody CoinDocument coinDocument) {
        CoinDocument createdDocument = coinService.saveCoinDocument(coinDocument);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all CoinDocuments")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CoinDocument.class)))))
    @GetMapping("/coins")
    public ResponseEntity<List<CoinDocument>> getAllCoinDocuments() {
        List<CoinDocument> documents = coinService.getAllCoinDocuments();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @Operation(summary = "Get CoinDocument by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CoinDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/coins/{id}")
    public ResponseEntity<CoinDocument> getCoinDocumentById(
            @Parameter(description = "CoinDocument ID", required = true) @PathVariable String id) {
        Optional<CoinDocument> document = coinService.getCoinDocumentById(id);
        return document.map(doc -> new ResponseEntity<>(doc, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Update CoinDocument by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = CoinDocument.class))),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/coins/{id}")
    public ResponseEntity<CoinDocument> updateCoinDocument(
            @Parameter(description = "CoinDocument ID", required = true) @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CoinDocument object", required = true, content = @Content(schema = @Schema(implementation = CoinDocument.class)))
            @RequestBody CoinDocument coinDocument) {
        Optional<CoinDocument> existingDocument = coinService.getCoinDocumentById(id);
        if (existingDocument.isPresent()) {
            coinDocument.setId(id);
            CoinDocument updatedDocument = coinService.saveCoinDocument(coinDocument);
            return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete CoinDocument by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/coins/{id}")
    public ResponseEntity<Void> deleteCoinDocumentById(
            @Parameter(description = "CoinDocument ID", required = true) @PathVariable String id) {
        Optional<CoinDocument> document = coinService.getCoinDocumentById(id);
        if (document.isPresent()) {
            coinService.deleteCoinDocumentById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ---------- Spin Processing ----------

    @Operation(summary = "Process a spin request", description = "Processes a roulette spin, updates the user's wallet, and returns the spin result with winnings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Spin processed successfully", content = @Content(schema = @Schema(implementation = SpinResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = SpinResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = SpinResponse.class)))
    })
    @PostMapping("/spin-request")
    public SpinResponse processSpin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "SpinRequest object", required = true, content = @Content(schema = @Schema(implementation = SpinRequest.class)))
            @RequestBody SpinRequest spinRequest) {
        SpinResponse response = new SpinResponse();
        response.setSpinStart(now());
        double betAmount = 0.0;
        double winAmount = 0.0;
        var id = generatePrimaryKey();
        var spinNumber = rouletteService.generateSpinNumber();
        int spinResult = RouletteGameLogics.getWheelResult();
        
        if (spinRequest == null || spinRequest.getBetsList() == null || spinRequest.getBetsList().isEmpty()) {
        	var betsList = new ArrayList<SpinResponse.Bet>();
        	var wonBetsList = new ArrayList<SpinResponse.WonBet>();
        	
            var uid = spinRequest.getUid();

            if (uid == null || uid.isEmpty() || userService.findOne(uid) == null) {
                response.setWallet(0.0);
                response.setOldCredit(0.0);
                response.setNewCredit(0.0);

            } else {
				var user = userService.findOne(uid);
					
				Double userWallet = user.getWallet();
		        response.setWallet(userWallet);
		        response.setOldCredit(userWallet);
		        response.setNewCredit(userWallet);

			}
            
            response.setId(id);
            response.setSpinNumber(spinNumber);
            response.setEgmId(spinRequest.getEgmId());
            response.setUid(uid);
            response.setBetsList(betsList);
            response.setWonBetsList(wonBetsList);
            response.setBetAmount(betAmount);
            response.setNumber(spinResult);
            response.setWinAmount(winAmount);
            response.setSpinEnd(now());
            response.setMsg("Spin completed successfully");
            response.setOk(1);
            response.setStatusCode(STATUS_OK);
            saveSpinResult(response, betsList, wonBetsList);
            return response;
        } else {
            betAmount = spinRequest.getBetsList().stream().mapToDouble(SpinRequest.Bet::getBetAmount).sum();
            var uid = spinRequest.getUid();

            if (uid == null || uid.isEmpty())
                return errorResponse(response, "Invalid request: User not found", STATUS_BAD_REQUEST);
            var user = userService.findOne(uid);
            if (user == null)
                return errorResponse(response, "Invalid request: User does not exist", STATUS_BAD_REQUEST);
            Double userWallet = user.getWallet();
            if (userWallet == null || userWallet <= 0 || userWallet < betAmount)
                return errorResponse(response, "Invalid request: User wallet is empty or insufficient", STATUS_BAD_REQUEST);
            List<SpinResponse.Bet> betsList = mapBetsToSpinResponse(spinRequest.getBetsList());
            List<SpinResponse.WonBet> wonBetsList = mapWonBetsToSpinResponse(spinRequest.getBetsList(), spinResult);
            winAmount = wonBetsList.stream().mapToDouble(SpinResponse.WonBet::getWinAmount).sum();
            user.setWallet(userWallet + winAmount - betAmount);
            user.setUpdatedAt(now());
            var updatedUser = userService.updateOne(uid, user);
            if (updatedUser == null) {
                user.setWallet(userWallet);
                userService.updateOne(uid, user);
                return errorResponse(response, "Error updating user wallet", STATUS_SERVER_ERROR);
            }
            response.setId(id);
            response.setSpinNumber(spinNumber);
            response.setEgmId(spinRequest.getEgmId());
            response.setUid(uid);
            response.setBetsList(betsList);
            response.setBetAmount(betAmount);
            response.setWallet(userWallet);
            response.setNumber(spinResult);
            response.setWinAmount(winAmount);
            response.setOldCredit(userWallet);
            response.setNewCredit(updatedUser.getWallet());
            response.setWonBetsList(wonBetsList);
            response.setSpinEnd(now());
            response.setMsg("Spin completed successfully");
            response.setOk(1);
            response.setStatusCode(STATUS_OK);
            saveSpinResult(response, betsList, wonBetsList);
            log.info("Spin completed: SpinNumber={}, BetAmount={}, CurrentWallet={}, SpinResult={}, UserWallet={}, WinAmount={}",
                    spinNumber, betAmount, userWallet, spinResult, updatedUser.getWallet(), winAmount);
            return response;

        }
    }

    // ---------- Internal Helpers ----------

    public static String generatePrimaryKey() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return now.format(formatter);
    }

    private Map<Integer, Long> groupByWinNumberFrequency(List<SpinResult> spinResults) {
        var spinResultsMap = spinResults.stream()
                .collect(Collectors.groupingBy(
                        SpinResult::getNumber,
                        Collectors.counting()
                ));
        for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++)
            spinResultsMap.putIfAbsent(i, 0L);
        return spinResultsMap;
    }

    private Map<Integer, Long> groupByWinNumberFrequencyPercent(List<SpinResult> spinResults) {
        var spinResultsMap = spinResults.stream()
                .collect(Collectors.groupingBy(
                        SpinResult::getNumber,
                        Collectors.counting()
                ));
        for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++)
            spinResultsMap.putIfAbsent(i, 0L);
        return spinResultsMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() * 100) / spinResults.size(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private Double calculatePercentage(Map<Integer, Long> winNumberFrequency, List<Integer> group, int totalSpins) {
        if (totalSpins == 0) return 0.0;
        Long groupFrequency = winNumberFrequency.entrySet()
                .stream()
                .filter(entry -> group.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .reduce(0L, Long::sum);
        Double calculatedPercentage = (groupFrequency * 100.0) / totalSpins;
        calculatedPercentage = Math.round(calculatedPercentage * 100.0) / 100.0;
        return calculatedPercentage;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private SpinResponse errorResponse(SpinResponse response, String msg, int statusCode) {
        response.setOk(0);
        response.setMsg(msg);
        response.setStatusCode(statusCode);
        response.setSpinEnd(now());
        return response;
    }

    private List<SpinResponse.Bet> mapBetsToSpinResponse(List<SpinRequest.Bet> bets) {
        return bets.stream().map(bet -> {
            SpinResponse.Bet spinBet = new SpinResponse.Bet();
            spinBet.setBetIndex(bet.getBetIndex());
            spinBet.setBetAmount(bet.getBetAmount());
            return spinBet;
        }).collect(Collectors.toList());
    }

    private List<SpinResponse.WonBet> mapWonBetsToSpinResponse(List<SpinRequest.Bet> bets, int spinResult) {
        return bets.stream()
                .filter(bet -> RouletteGameLogics.isBetWon(bet.getBetIndex(), spinResult))
                .map(bet -> {
                    SpinResponse.WonBet wonBet = new SpinResponse.WonBet();
                    wonBet.setBetIndex(bet.getBetIndex());
                    wonBet.setBetAmount(bet.getBetAmount());
                    wonBet.setWinAmount(RouletteGameLogics.calculateWinAmount(bet.getBetIndex(), bet.getBetAmount(), spinResult));
                    wonBet.setName(RouletteGameLogics.getBetName(bet.getBetIndex()));
                    return wonBet;
                }).collect(Collectors.toList());
    }

    private void saveSpinResult(
            SpinResponse response,
            List<SpinResponse.Bet> betsList,
            List<SpinResponse.WonBet> wonBetsList
    ) {
        SpinResult spinResultObj = new SpinResult();
        spinResultObj.setId(response.getId());
        spinResultObj.setSpinNumber(response.getSpinNumber());
        spinResultObj.setEgmId(response.getEgmId());
        spinResultObj.setUid(response.getUid());
        spinResultObj.setBetsList(mapBetsToSpinResult(betsList));
        spinResultObj.setBetAmount(response.getBetAmount());
        spinResultObj.setNumber(response.getNumber());
        spinResultObj.setWallet(response.getWallet());
        spinResultObj.setWinAmount(response.getWinAmount());
        spinResultObj.setOldCredit(response.getOldCredit());
        spinResultObj.setNewCredit(response.getNewCredit());
        spinResultObj.setWonBetsList(mapWonBetsToSpinResult(wonBetsList));
        spinResultObj.setSpinStart(response.getSpinStart());
        spinResultObj.setSpinEnd(response.getSpinEnd());
        spinResultService.saveSpinResult(spinResultObj);
    }

    private List<SpinResult.Bet> mapBetsToSpinResult(List<SpinResponse.Bet> bets) {
        return bets.stream().map(bet -> {
            SpinResult.Bet spinBet = new SpinResult.Bet();
            spinBet.setBetIndex(bet.getBetIndex());
            spinBet.setBetAmount(bet.getBetAmount());
            return spinBet;
        }).collect(Collectors.toList());
    }

    private List<SpinResult.WonBet> mapWonBetsToSpinResult(List<SpinResponse.WonBet> wonBets) {
        return wonBets.stream().map(wonBet -> {
            SpinResult.WonBet spinWonBet = new SpinResult.WonBet();
            spinWonBet.setBetIndex(wonBet.getBetIndex());
            spinWonBet.setBetAmount(wonBet.getBetAmount());
            spinWonBet.setWinAmount(wonBet.getWinAmount());
            spinWonBet.setName(wonBet.getName());
            return spinWonBet;
        }).collect(Collectors.toList());
    }
}