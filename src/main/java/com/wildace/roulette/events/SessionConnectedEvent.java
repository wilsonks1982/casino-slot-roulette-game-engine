package com.wildace.roulette.events;

import java.time.Instant;

public final class SessionConnectedEvent {
    private final String sessionId;
    private final String uid;
    private final String egmId;
    private final String tableId;
    private final Instant occurredAt;

    public SessionConnectedEvent(String sessionId, String uid, String egmId, String tableId) {
        this.sessionId = sessionId;
        this.uid = uid;
        this.egmId = egmId;
        this.tableId = tableId;
        this.occurredAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public String getUid() { return uid; }
    public String getEgmId() { return egmId; }
    public String getTableId() { return tableId; }
    public Instant getOccurredAt() { return occurredAt; }
}