package com.wildace.roulette.services;

import com.wildace.roulette.events.Bet;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayoutSettlementService {

    private final InMemoryBetStore betStore;
    private final UserService userService;

    public PayoutSettlementService(InMemoryBetStore betStore, UserService userService) {
        this.betStore = betStore;
        this.userService = userService;
    }

    /**
     * Settle a table+round and credit winners.
     *
     * Assumption:
     * - Stake was already debited at PLACE_BET time.
     * - So we only CREDIT total winning payouts (including returned stake on winning bets).
     */
    public RoundSettlement settleRound(String tableId, String roundId, int winningNumber, boolean clearAfter) {
        if (tableId == null || tableId.isBlank()) throw new IllegalArgumentException("tableId is required");
        if (roundId == null || roundId.isBlank()) throw new IllegalArgumentException("roundId is required");

        Map<String, List<Bet>> betsByUid = betStore.getBetsByUid(tableId, roundId);

        double totalStake = 0.0;
        double totalPayout = 0.0;
        int totalPlayers = 0;

        for (var entry : betsByUid.entrySet()) {
            String uid = entry.getKey();
            List<Bet> bets = entry.getValue();
            if (bets == null || bets.isEmpty()) continue;

            totalPlayers++;

            UserSettlement settlement = settleUser(uid, bets, winningNumber, roundId);

            totalStake += settlement.totalStake;
            totalPayout += settlement.totalPayout;
        }

        if (clearAfter) {
            betStore.clearRound(tableId, roundId);
        }

        return new RoundSettlement(
                tableId,
                roundId,
                winningNumber,
                totalPlayers,
                totalStake,
                totalPayout,
                totalPayout - totalStake
        );
    }

    /**
     * Settle one user's bets and credit wallet with totalPayout.
     */
    public UserSettlement settleUser(String uid, List<Bet> bets, int winningNumber, String roundId) {
        if (uid == null || uid.isBlank()) throw new IllegalArgumentException("uid is required");
        if (bets == null) bets = List.of();

        double totalStake = 0.0;
        double totalPayout = 0.0;

        for (Bet bet : bets) {
            if (bet == null) continue;

            Integer betIndex = bet.getBetIndex();
            Integer betAmountInt = bet.getBetAmount();

            int betAmount = betAmountInt != null ? betAmountInt : 0;
            if (betAmount <= 0) continue;

            totalStake += betAmount;

            if (betIndex == null) continue;

            // payout INCLUDING stake if won; else 0
            totalPayout += RouletteGameLogics.calculateWinAmount(betIndex, betAmount, winningNumber);
        }

        boolean credited = userService.creditWallet(uid, totalPayout, roundId, "Round settled: " + totalPayout);
        if (!credited) {
            log.warn("Failed to credit payout uid={}, amount={}", uid, totalPayout);
        }

        return new UserSettlement(uid, totalStake, totalPayout);
    }

    @Value
    public static class UserSettlement {
        String uid;
        double totalStake;
        double totalPayout;
    }

    @Value
    public static class RoundSettlement {
        String tableId;
        String roundId;
        int winningNumber;
        int totalPlayers;
        double totalStake;
        double totalPayout;
        double netResult; // reporting only
    }
}