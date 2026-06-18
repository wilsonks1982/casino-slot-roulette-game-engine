package com.wildace.roulette.websocket;

import com.wildace.roulette.domain.documents.SpinResult;
import com.wildace.roulette.domain.documents.WheelResult;
import com.wildace.roulette.events.*;
import com.wildace.roulette.repositories.WheelResultRepository;
import com.wildace.roulette.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouletteEngineRng {
    // Why ScheduledExecutorService? The ScheduledExecutorService is used to schedule tasks to run after a delay or periodically.
    // In the context of a roulette game, it can be used to manage the timing of game phases (e.g., opening and closing betting,
    // spinning the wheel, etc.) without blocking the main thread, allowing for a responsive user experience.
    //What is Executors.newSingleThreadScheduledExecutor()?
    // Executors.newSingleThreadScheduledExecutor() creates a ScheduledExecutorService that uses a single worker thread to execute tasks.
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final String tableId = "1";

    private final ConnectionRegistry registry;
    private final WheelResultRepository wheelResultRepository;
    private final UserService userService;
    private final PayoutSettlementService payoutSettlementService;
    private final InMemoryBetStore betStore;

    // store the winning number for the current round so payout uses the same value
    private volatile Integer lastWinningNumber = null;

    // Internal-only engine state machine.
    public enum GameState {
        WAITING,
        ROUND_CREATED,
        BETTING_OPEN,
        WHEEL_SPINNING,
        BALL_RELEASED,
        BETTING_CLOSED,
        RESULT_DECLARED,
        PAYOUT_COMPLETED
    }

    //Why volatile?
    // The volatile keyword is used to ensure that changes to the state and roundId variables are immediately visible to all threads.
    private volatile GameState state = GameState.WAITING;
    // round id intialise with epoch time in seconds
    //int is of 4 bytes and can hold values from -2,147,483,648 to 2,147,483,647.
    // Using epoch time in seconds allows for a unique round ID that increments with each new round,
    // ensuring that each round can be easily identified and tracked without the risk of overflow
    // for a long time (until the year 2038 problem occurs).
    // This approach also simplifies the generation of round IDs without needing additional logic to manage increments or resets.
    private volatile int roundId = 60 + (int) (System.currentTimeMillis() / 1000);

    public RouletteEngineRng(ConnectionRegistry registry, WheelResultRepository wheelResultRepository, UserService userService, PayoutSettlementService payoutSettlementService, InMemoryBetStore betStore) {
        this.registry = registry;
        this.wheelResultRepository = wheelResultRepository;
        this.userService = userService;
        this.payoutSettlementService = payoutSettlementService;
        this.betStore = betStore;
        startGameLoop();
    }

    private void startGameLoop() {
        scheduler.scheduleAtFixedRate(this::startRound, 2, 35, TimeUnit.SECONDS);
    }

    private void startRound() {
        roundId++;
        lastWinningNumber = null;
        roundCreated();
        scheduler.schedule(this::openBetting, 1, TimeUnit.SECONDS);
        scheduler.schedule(this::spinWheel, 10, TimeUnit.SECONDS);
        scheduler.schedule(this::ballReleased, 13, TimeUnit.SECONDS);
        scheduler.schedule(this::closeBetting, 15, TimeUnit.SECONDS);
        scheduler.schedule(this::announceResult, 27, TimeUnit.SECONDS);
        scheduler.schedule(this::payout, 30, TimeUnit.SECONDS);
    }

    private void roundCreated() {
        state = GameState.ROUND_CREATED;
        var event = RoundCreatedMessage.builder()
                .type("ROUND_CREATED")
                .messageId(roundId + "11")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(RoundCreated.builder()
                        .roundId(Integer.toString(roundId))
                        .tableId(tableId)
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
        log.info("Round created event broadcasted for round: {} on table {}", roundId, tableId);
    }

    private void openBetting() {
        state = GameState.BETTING_OPEN;
        var event = BettingOpenedMessage.builder()
                .type("BETTING_OPENED")
                .messageId(roundId + "12")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(BettingOpened.builder()
                        .roundId(Integer.toString(roundId))
                        .duration(15)
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
    }


    private void spinWheel() {
        state = GameState.WHEEL_SPINNING;
        var event = WheelSpinningMessage.builder()
                .type("WHEEL_SPINNING")
                .messageId(roundId + "13")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(WheelSpinning.builder()
                        .roundId(Integer.toString(roundId))
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
    }

    private void ballReleased() {
        state = GameState.BALL_RELEASED;
        var event = BallReleasedMessage.builder()
                .type("BALL_RELEASED")
                .messageId(roundId + "14")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(BallReleased.builder()
                        .roundId(Integer.toString(roundId))
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
    }

    private void closeBetting() {
        state = GameState.BETTING_CLOSED;

        String roundIdStr = Integer.toString(roundId);

        // total stake for this table+round across all players
        double totalBets = betStore.getBetsByUid(tableId, roundIdStr).values().stream()
                .flatMap(List::stream)
                .filter(b -> b != null && b.getBetAmount() != null)
                .mapToDouble(b -> b.getBetAmount())
                .sum();

        var event = BettingClosedMessage.builder()
                .type("BETTING_CLOSED")
                .messageId(roundId + "15")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(BettingClosed.builder()
                        .roundId(roundIdStr)
                        .totalBets(totalBets)
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
    }


    private void announceResult() {

        int number = RouletteGameLogics.getWheelResult();
        lastWinningNumber = number;

        WheelResult wheelResult = WheelResult.builder()
                .roundId(Integer.toString(roundId))
                .tableId(tableId)
                .number(number)
                .color(RouletteGameLogics.BLACK.contains(number) ? "BLACK": RouletteGameLogics.RED.contains(number) ? "RED" : "GREEN")
                .parity(RouletteGameLogics.EVEN.contains(number) ? "EVEN" : RouletteGameLogics.ODD.contains(number) ? "ODD" : "NONE")
                .column(RouletteGameLogics.getColumn(number))
                .dozen(RouletteGameLogics.getDozen(number))
                .build();
        wheelResultRepository.save(wheelResult);

        List<WheelResult> currentResults= wheelResultRepository.findAllByTableId(tableId); ;
        List<Integer> hotNumbers = getHotNumbers(currentResults);
        List<Integer> coldNumbers = getColdNumbers(currentResults);
        List<Stat> statistics = getStatistics(currentResults);
        Map<String, Double> groups = getGroups(currentResults);
        List<Win> lastNumbers = currentResults.stream()
                .sorted((r1, r2) -> r2.getRoundId().compareTo(r1.getRoundId()))
                .map(result -> Win.builder()
                        .winningNumber(result.getNumber())
                        .roundId(result.getRoundId())
                        .build())
                .toList();


        state = GameState.RESULT_DECLARED;
        var event = ResultDeclaredMessage.builder()
                .type("RESULT_DECLARED")
                .messageId(roundId + "16")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(ResultDeclared.builder()
                        .roundId(Integer.toString(roundId))
                        .winningNumber(number)
                        .color(RouletteGameLogics.BLACK.contains(number) ? "BLACK": RouletteGameLogics.RED.contains(number) ? "RED" : "GREEN")
                        .parity(RouletteGameLogics.EVEN.contains(number) ? "EVEN" : RouletteGameLogics.ODD.contains(number) ? "ODD" : "NONE")
                        .column(RouletteGameLogics.getColumn(number))
                        .dozen(RouletteGameLogics.getDozen(number))
                        .hotNumbers(hotNumbers)
                        .coldNumbers(coldNumbers)
                        .statistics(statistics)
                        .groups(groups)
                        .lastNumbers(lastNumbers)
                        .build())
                .build();
        registry.broadcastToTable(tableId, event);
        log.info(event.getPayload().toString());
    }

    private void payout() {
        state = GameState.PAYOUT_COMPLETED;

        String roundIdStr = Integer.toString(roundId);

        if (lastWinningNumber == null) {
            log.warn("Skipping payout because lastWinningNumber is null (roundId={}, tableId={})", roundIdStr, tableId);
            return;
        }

        var settlement = payoutSettlementService.settleRound(
                tableId,
                roundIdStr,
                lastWinningNumber,
                true // clear bets for this round after settling
        );

        var event = PayoutCompletedMessage.builder()
                .type("PAYOUT_COMPLETED")
                .messageId(roundId + "17")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(PayoutCompleted.builder()
                        .roundId(roundIdStr)
                        .totalStake(settlement.getTotalStake())
                        .totalPayout(settlement.getTotalPayout())
                        .netResult(settlement.getNetResult())
                        .build())
                .build();

        registry.broadcastToTable(tableId, event);

        log.info("Payout completed: roundId={}, tableId={}, players={}, stake={}, payout={}, net={}",
                roundIdStr,
                tableId,
                settlement.getTotalPlayers(),
                settlement.getTotalStake(),
                settlement.getTotalPayout(),
                settlement.getNetResult()
        );
    }


    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        log.info("Received SessionConnectedEvent for session: {}, EGM: {}, UID: {}, Table: {}",
                event.getSessionId(), event.getEgmId(), event.getUid(), event.getTableId());

        WebSocketSession session = registry.getSessionById(event.getSessionId());
        if (session == null) {
            log.warn("WebSocketSession not found for session ID: {}", event.getSessionId());
            return;
        }

        // Prefer session attribute if that is the source of truth
        String sessionTableId = (String) session.getAttributes().get("tableId");
        // fallback to event tableId if needed
        if (sessionTableId == null) sessionTableId = event.getTableId();

        if (!tableId.equals(sessionTableId)) {
            log.info("Skipping INITIAL_DATA for session {} because sessionTableId={} != engineTableId={}",
                    session.getId(), sessionTableId, tableId);
            return;
        }

        sendInitialState(session);
    }

    public void sendInitialState(WebSocketSession session) {

        String uid = (String) session.getAttributes().get("uid");

        Double balance = 0.0;
        var user = userService.findOne(uid);
        if (user != null && user.getWallet() != null) {
            balance = user.getWallet();
        }

        var walletData = Wallet.builder()
                .balance(balance)
                .build();

        var tableInfoData = TableInfo.builder()
                .tableId(tableId)
                .tableName("Roulette Table " + tableId)
                .wheelType("European") // Example wheel type, can be dynamic based on actual table configuration
                .build();

        var payoutData = Payout.builder()
                .build();

        var tableLimitData = TableLimit.builder()
                .build();
        var sessionData = Session.builder()
                .sessionId(session.getId())
                .egmId((String) session.getAttributes().get("egmId"))
                .uid((String) session.getAttributes().get("uid"))
                .build();

        var gameStateData = com.wildace.roulette.events.GameState.builder()
                .roundId(Integer.toString(roundId))
                .status(state.name())
                .secondsRemaining(-1)
                .build();

        String roundIdStr = Integer.toString(roundId);

        List<Bet> myBets = betStore.getBets(tableId, roundIdStr, uid);

        double totalStake = myBets.stream()
                .filter(b -> b != null && b.getBetAmount() != null)
                .mapToDouble(b -> b.getBetAmount())
                .sum();

        var currentBetsData = CurrentBets.builder()
                .totalStake(totalStake)
                .bets(myBets)
                .build();

        List<WheelResult> currentResults= wheelResultRepository.findAllByTableId(tableId); ;
        List<Integer> hotNumbers = getHotNumbers(currentResults);
        List<Integer> coldNumbers = getColdNumbers(currentResults);
        List<Stat> statistics = getStatistics(currentResults);
        Map<String, Double> groups = getGroups(currentResults);
        List<Win> lastNumbers = currentResults.stream()
                .sorted((r1, r2) -> r2.getRoundId().compareTo(r1.getRoundId()))
                .map(result -> Win.builder()
                        .winningNumber(result.getNumber())
                        .roundId(result.getRoundId())
                        .build())
                .toList();

        var historyData = History.builder()
                .hotNumbers(HotNumbers.builder().hotNumbers(hotNumbers).build())
                .coldNumbers(ColdNumbers.builder().coldNumbers(coldNumbers).build())
                .lastNumbers(LastNumbers.builder().lastNumbers(lastNumbers).build())
                .group(Group.builder().groupBlack(groups.get("GroupBlack"))
                        .groupRed(groups.get("GroupRed"))
                        .groupEven(groups.get("GroupEven"))
                        .groupOdd(groups.get("GroupOdd"))
                        .group1to18(groups.get("Group1to18"))
                        .group19to36(groups.get("Group19to36"))
                        .groupFirstLine(groups.get("GroupColumn1"))
                        .groupSecondLine(groups.get("GroupColumn2"))
                        .groupThirdLine(groups.get("GroupColumn3"))
                        .group1to12(groups.get("GroupDozen1"))
                        .group13to24(groups.get("GroupDozen2"))
                        .group25to36(groups.get("GroupDozen3"))
                        .build())
                .statistics(Statistics.builder().statistics(statistics).build())
                .build();


        var event = InitialDataMessage.builder()
                .type("INITIAL_DATA")
                .messageId(roundId + "10")
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(InitialData.builder()
                        .tableInfo(tableInfoData)
                        .payout(payoutData)
                        .tableLimit(tableLimitData)
                        .session(sessionData)
                        .wallet(walletData)
                        .gameState(gameStateData)
                        .currentBets(currentBetsData)
                        .history(historyData)
                        .build()
                )
                .build();
        registry.send(session, event);
    }

    private Map<Integer, Long> groupByWinNumberFrequency(List<WheelResult> spinResults) {
        var spinResultsMap = spinResults.stream()
                .collect(Collectors.groupingBy(
                        WheelResult::getNumber,
                        Collectors.counting()
                ));
        for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++)
            spinResultsMap.putIfAbsent(i, 0L);
        return spinResultsMap;
    }

    private Map<Integer, Long> groupByWinNumberFrequencyPercent(List<WheelResult> spinResults) {
        var spinResultsMap = spinResults.stream()
                .collect(Collectors.groupingBy(
                        WheelResult::getNumber,
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

    private List<Stat> getStatistics(List<WheelResult> wheelResults) {
        if (wheelResults == null || wheelResults.isEmpty()) {
            var emptyStatistics = new LinkedHashMap<Integer, Stat>();
            for (int i = 0; i <= RouletteGameLogics.MAX_POCKETS; i++) {
                emptyStatistics.put(i, Stat.builder()
                        .count(0)
                        .percentage(0.0)
                        .build());
            }
            return emptyStatistics.entrySet()
                    .stream()
                    .map(entry -> entry.getValue())
                    .collect(Collectors.toList());
        }
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(wheelResults);
        Map<Integer, Long> winNumberFrequencyPercent = groupByWinNumberFrequencyPercent(wheelResults);
        return winNumberFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> Stat.builder()
                        .count(entry.getValue().intValue())
                        .percentage(winNumberFrequencyPercent.get(entry.getKey()).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, Double> getGroups(List<WheelResult> wheelResults) {
        if (wheelResults.isEmpty()) {
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
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(wheelResults);
        Map<String, Double> groupPercentages = new LinkedHashMap<>();
        groupPercentages.put("GroupRed", calculatePercentage(winNumberFrequency, RouletteGameLogics.RED, wheelResults.size()));
        groupPercentages.put("GroupBlack", calculatePercentage(winNumberFrequency, RouletteGameLogics.BLACK, wheelResults.size()));
        groupPercentages.put("GroupOdd", calculatePercentage(winNumberFrequency, RouletteGameLogics.ODD, wheelResults.size()));
        groupPercentages.put("GroupEven", calculatePercentage(winNumberFrequency, RouletteGameLogics.EVEN, wheelResults.size()));
        groupPercentages.put("Group1to18", calculatePercentage(winNumberFrequency, RouletteGameLogics.LOW, wheelResults.size()));
        groupPercentages.put("Group19to36", calculatePercentage(winNumberFrequency, RouletteGameLogics.HIGH, wheelResults.size()));
        groupPercentages.put("GroupDozen1", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN1, wheelResults.size()));
        groupPercentages.put("GroupDozen2", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN2, wheelResults.size()));
        groupPercentages.put("GroupDozen3", calculatePercentage(winNumberFrequency, RouletteGameLogics.DOZEN3, wheelResults.size()));
        groupPercentages.put("GroupColumn1", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN1, wheelResults.size()));
        groupPercentages.put("GroupColumn2", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN2, wheelResults.size()));
        groupPercentages.put("GroupColumn3", calculatePercentage(winNumberFrequency, RouletteGameLogics.COLUMN3, wheelResults.size()));
        return groupPercentages;
    }

    private List<Integer> getHotNumbers(List<WheelResult> wheelResults) {
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(wheelResults);
        return winNumberFrequency.entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Integer> getColdNumbers(List<WheelResult> wheelResults) {
        Map<Integer, Long> winNumberFrequency = groupByWinNumberFrequency(wheelResults);
        return winNumberFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


}
