package com.wildace.roulette.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildace.roulette.domain.documents.User;
import com.wildace.roulette.events.BetPlacedMessage;
import com.wildace.roulette.events.SessionErrorEvent;
import com.wildace.roulette.websocket.ConnectionRegistry;
import com.wildace.roulette.websocket.GameMessage;
import com.wildace.roulette.websocket.PlaceBetPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TerminalSessionApplicationService {

    private final ObjectMapper objectMapper;
    private final ConnectionRegistry registry;
    private final ApplicationEventPublisher publisher;
    private final InMemoryBetStore betStore;
    private final UserService userService;
    private final BetIdGenerator betIdGenerator;

    // ==================== Connection Handlers ====================

    public TerminalSessionApplicationService(ObjectMapper objectMapper, ConnectionRegistry registry, ApplicationEventPublisher publisher, InMemoryBetStore betStore, UserService userService, BetIdGenerator betIdGenerator) {
        this.objectMapper = objectMapper;
        this.registry = registry;
        this.publisher = publisher;
        this.betStore = betStore;
        this.userService = userService;
        this.betIdGenerator = betIdGenerator;
    }

    public void handlePlaceBet(WebSocketSession session, GameMessage gameMessage) {
        String uid = (String) session.getAttributes().get("uid");
        String tableId = (String) session.getAttributes().get("tableId");

        PlaceBetPayload payload = objectMapper.convertValue(gameMessage.payload(), PlaceBetPayload.class);

        // Generate betId early so even rejections include it
        String betId = betIdGenerator.generate(uid, payload.roundId());

        int stake = payload.stake() != null ? payload.stake() : 0;
        if (stake <= 0) {
            rejectBet(session, payload, betId, "Invalid stake");
            return;
        }

        int computedStake = payload.betsList() == null ? 0 :
                payload.betsList().stream()
                        .filter(b -> b != null && b.getBetAmount() != null)
                        .mapToInt(b -> b.getBetAmount())
                        .sum();

        if (computedStake != stake) {
            rejectBet(session, payload, betId, "Stake mismatch");
            return;
        }

        boolean debited = userService.debitWallet(uid, stake, payload.roundId(), "Wallet debited: " + stake);
        if (!debited) {
            rejectBet(session, payload, betId, "Insufficient balance");
            return;
        }

        betStore.addBets(tableId, payload.roundId(), uid, payload.betsList());

        User user = userService.findOne(uid);
        Double newBalance = (user != null && user.getWallet() != null) ? user.getWallet() : 0.0;

        var betPlaced = BetPlacedMessage.builder()
                .type("BET_PLACED")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(BetPlacedMessage.BetPlaced.builder()
                        .roundId(payload.roundId())
                        .betId(betId)
                        .stake(stake)
                        .betsList(payload.betsList())
                        .newBalance(newBalance)
                        .build())
                .build();

        registry.send(session, betPlaced);

        log.info("BET_PLACED sent uid={}, tableId={}, roundId={}, stake={}, newBalance={}",
                uid, tableId, payload.roundId(), stake, newBalance);
    }


    private void rejectBet(WebSocketSession session, PlaceBetPayload payload, String betId, String reason) {
        String uid = (String) session.getAttributes().get("uid");

        Double newBalance = 0.0;
        User user = userService.findOne(uid);
        if (user != null && user.getWallet() != null) {
            newBalance = user.getWallet();
        }

        var rejected = com.wildace.roulette.events.BetRejectedMessage.builder()
                .type("BET_REJECTED")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(com.wildace.roulette.events.BetRejectedMessage.BetRejected.builder()
                        .roundId(payload != null ? payload.roundId() : null)
                        .betId(betId)
                        .stake(payload != null ? payload.stake() : null)
                        .betsList(payload != null ? payload.betsList() : null)
                        .reason(reason)
                        .newBalance(newBalance)
                        .build())
                .build();

        registry.send(session, rejected);

        log.warn("BET_REJECTED sent sessionId={}, uid={}, roundId={}, betId={}, reason={}",
                session.getId(),
                uid,
                payload != null ? payload.roundId() : null,
                betId,
                reason
        );

        publisher.publishEvent(new SessionErrorEvent(session.getId(), "BET_REJECTED: " + reason));
    }


    public void handleHeartbeat(WebSocketSession session) {
        // This can be used to update last active timestamp or perform other session maintenance
        log.debug("Received heartbeat from session: {}", session.getId());
    }

    // ==================== Unicast Messages ====================

    /**
     * Send a message to a specific session by sessionId
     */
    public void unicastBySessionId(String sessionId, Object message) {
        try {
            WebSocketSession session = registry.getSession(sessionId);
            if (session != null && session.isOpen()) {
                unicastMessage(session, message);
            } else {
                log.warn("Session not found or closed: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error sending unicast message to session: {}", sessionId, e);
        }
    }

    /**
     * Send a message to a specific terminal by egmId
     */
    public void unicastByEgmId(String egmId, Object message) {
        try {
            WebSocketSession session = registry.getSessionByEgmId(egmId);
            if (session != null && session.isOpen()) {
                unicastMessage(session, message);
            } else {
                log.warn("Session not found for egmId: {}", egmId);
            }
        } catch (Exception e) {
            log.error("Error sending unicast message to egmId: {}", egmId, e);
        }
    }

    /**
     * Send a message to a specific user by uid
     */
    public void unicastByUid(String uid, Object message) {
        try {
            WebSocketSession session = registry.getSessionByUid(uid);
            if (session != null && session.isOpen()) {
                unicastMessage(session, message);
            } else {
                log.warn("Session not found for uid: {}", uid);
            }
        } catch (Exception e) {
            log.error("Error sending unicast message to uid: {}", uid, e);
        }
    }

    /**
     * Send a message to a specific table by tableId
     */
    public void unicastByTableId(String tableId, Object message) {
        try {
            WebSocketSession session = registry.getSessionByTableId(tableId);
            if (session != null && session.isOpen()) {
                unicastMessage(session, message);
            } else {
                log.warn("Session not found for tableId: {}", tableId);
            }
        } catch (Exception e) {
            log.error("Error sending unicast message to tableId: {}", tableId, e);
        }
    }

    /**
     * Send a message to a specific WebSocket session
     */
    public void unicastMessage(WebSocketSession session, Object message) {
        try {
            if (session != null && session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("Unicast message sent to session: {}", session.getId());
            }
        } catch (Exception e) {
            log.error("Error sending unicast message to session: {}",
                    session != null ? session.getId() : "null", e);
        }
    }

    // ==================== Broadcast Messages ====================

    /**
     * Broadcast a message to all connected terminals
     */
    public void broadcastToAll(Object message) {
        try {
            int count = 0;
            for (WebSocketSession session : getAllActiveSessions()) {
                if (session != null && session.isOpen()) {
                    unicastMessage(session, message);
                    count++;
                }
            }
            log.info("Broadcasted message to {} terminals", count);
        } catch (Exception e) {
            log.error("Error broadcasting message to all", e);
        }
    }

    /**
     * Broadcast a message to all terminals on a specific table
     */
    public void broadcastToTable(String tableId, Object message) {
        try {
            int count = 0;
            for (WebSocketSession session : getAllActiveSessions()) {
                if (session != null && session.isOpen()) {
                    ConnectionRegistry.SessionMetadata metadata = registry.getMetadata(session.getId());
                    if (metadata != null && tableId.equals(metadata.getTableId())) {
                        unicastMessage(session, message);
                        count++;
                    }
                }
            }
            log.info("Broadcasted message to {} terminals on table: {}", count, tableId);
        } catch (Exception e) {
            log.error("Error broadcasting message to table: {}", tableId, e);
        }
    }

    /**
     * Broadcast a message to all terminals except the specified one
     */
    public void broadcastExcept(String excludeSessionId, Object message) {
        try {
            int count = 0;
            for (WebSocketSession session : getAllActiveSessions()) {
                if (session != null && session.isOpen() && !session.getId().equals(excludeSessionId)) {
                    unicastMessage(session, message);
                    count++;
                }
            }
            log.info("Broadcasted message to {} terminals (excluding session: {})", count, excludeSessionId);
        } catch (Exception e) {
            log.error("Error broadcasting message except session: {}", excludeSessionId, e);
        }
    }

    /**
     * Broadcast a message to all terminals except those on a specific table
     */
    public void broadcastExceptTable(String tableId, Object message) {
        try {
            int count = 0;
            for (WebSocketSession session : getAllActiveSessions()) {
                if (session != null && session.isOpen()) {
                    ConnectionRegistry.SessionMetadata metadata = registry.getMetadata(session.getId());
                    if (metadata != null && !tableId.equals(metadata.getTableId())) {
                        unicastMessage(session, message);
                        count++;
                    }
                }
            }
            log.info("Broadcasted message to {} terminals (excluding table: {})", count, tableId);
        } catch (Exception e) {
            log.error("Error broadcasting message except table: {}", tableId, e);
        }
    }

    /**
     * Broadcast a message to multiple specific terminals by egmId
     */
    public void broadcastToEgmIds(java.util.List<String> egmIds, Object message) {
        try {
            int count = 0;
            for (String egmId : egmIds) {
                WebSocketSession session = registry.getSessionByEgmId(egmId);
                if (session != null && session.isOpen()) {
                    unicastMessage(session, message);
                    count++;
                }
            }
            log.info("Broadcasted message to {} egmIds", count);
        } catch (Exception e) {
            log.error("Error broadcasting message to egmIds", e);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get all active WebSocket sessions
     */
    private Collection<WebSocketSession> getAllActiveSessions() {
        // This is a helper that would need to be added to ConnectionRegistry
        // For now, returning empty - implement based on your registry structure
        ConcurrentHashMap<String, ConnectionRegistry.SessionMetadata> sessions =
                new ConcurrentHashMap<>();
        return new java.util.ArrayList<>();
    }

    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return registry.getActiveSessionsCount();
    }

    /**
     * Check if a session is active
     */
    public boolean isSessionActive(String sessionId) {
        return registry.isRegistered(sessionId);
    }
}