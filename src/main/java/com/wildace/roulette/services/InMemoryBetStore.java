package com.wildace.roulette.services;

import com.wildace.roulette.events.Bet;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryBetStore {

    private record Key(String tableId, String roundId, String uid) {}

    private final ConcurrentHashMap<Key, List<Bet>> store = new ConcurrentHashMap<>();

    /**
     * Returns bets for a specific user for a round.
     * Never returns null.
     */
    public List<Bet> getBets(String tableId, String roundId, String uid) {
        if (tableId == null || roundId == null || uid == null) return List.of();
        return store.getOrDefault(new Key(tableId, roundId, uid), List.of());
    }

    public void addBets(String tableId, String roundId, String uid, List<Bet> bets) {
        if (tableId == null || roundId == null || uid == null) return;
        if (bets == null || bets.isEmpty()) return;

        Key key = new Key(tableId, roundId, uid);

        store.compute(key, (k, existing) -> {
            List<Bet> merged = new ArrayList<>(existing != null ? existing : List.of());
            merged.addAll(bets);
            return Collections.unmodifiableList(merged);
        });
    }

    public Map<String, List<Bet>> getBetsByUid(String tableId, String roundId) {
        Map<String, List<Bet>> result = new HashMap<>();
        for (var entry : store.entrySet()) {
            Key k = entry.getKey();
            if (k.tableId.equals(tableId) && k.roundId.equals(roundId)) {
                result.put(k.uid, entry.getValue());
            }
        }
        return result;
    }

    public void clearRound(String tableId, String roundId) {
        store.keySet().removeIf(k -> k.tableId.equals(tableId) && k.roundId.equals(roundId));
    }
}
