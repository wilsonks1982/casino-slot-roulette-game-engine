package com.wildace.roulette.services;

import com.wildace.roulette.events.BalanceUpdatedMessage;
import com.wildace.roulette.events.UserBalanceUpdatedEvent;
import com.wildace.roulette.websocket.ConnectionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.time.ZoneId;

@Component
@Slf4j
public class UserBalanceUpdatedEventListener {

    private final ConnectionRegistry registry;

    public UserBalanceUpdatedEventListener(ConnectionRegistry registry) {
        this.registry = registry;
    }

    @EventListener
    public void onUserBalanceUpdated(UserBalanceUpdatedEvent event) {
        var msg = BalanceUpdatedMessage.builder()
                .type("BALANCE_UPDATED")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .timestamp(String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Kolkata"))))
                .payload(BalanceUpdatedMessage.Payload.builder()
                        .roundId(event.getRoundId())
                        .newBalance(event.getNewBalance())
                        .reason(event.getReason())
                        .build())
                .build();

        WebSocketSession session = registry.getSessionByUid(event.getUid());
        if (session != null && session.isOpen()) {
            registry.send(session, msg);
        } else {
            log.debug("No open session for uid={} (BALANCE_UPDATED not sent)", event.getUid());
        }
    }
}