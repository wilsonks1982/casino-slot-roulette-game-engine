package com.wildace.roulette.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectionRegistry {


    // Session metadata wrapper
    @Data
    @AllArgsConstructor
    public static class SessionMetadata {
        private String sessionId;
        private String egmId;
        private String uid;
        private String tableId;
        private WebSocketSession webSocketSession;
        private long connectedAt;
    }

    // Store sessions with metadata
    private final ConcurrentHashMap<String, SessionMetadata> sessions = new ConcurrentHashMap<>();

    // Index by egmId for quick lookup
    private final ConcurrentHashMap<String, String> egmIdIndex = new ConcurrentHashMap<>();

    // Index by uid for quick lookup
    private final ConcurrentHashMap<String, String> uidIndex = new ConcurrentHashMap<>();

    // Index by tableId for quick lookup
    private final ConcurrentHashMap<String, String> tableIdIndex = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void register(WebSocketSession session) {
        try {
            // Extract query parameters from WebSocket URI
            String egmId = extractQueryParameter(session, "egmId");
            String uid = extractQueryParameter(session, "uid");
            String tableId = extractQueryParameter(session, "tableId");

            // Set default tableId if missing
            if (tableId == null || tableId.isEmpty()) {
                tableId = "1";
            }

            if (egmId == null || uid == null) {
                log.warn("Missing egmId or uid in WebSocket connection");
                return;
            }

            // Create metadata
            SessionMetadata metadata = new SessionMetadata(
                    session.getId(),
                    egmId,
                    uid,
                    tableId,
                    session,
                    System.currentTimeMillis()
            );

            // Store session with metadata
            sessions.put(session.getId(), metadata);

            // Create indexes
            egmIdIndex.put(egmId, session.getId());
            uidIndex.put(uid, session.getId());
            tableIdIndex.put(tableId, session.getId());

            log.info("Registered WebSocket session - egmId: {}, uid: {}, tableId: {}, sessionId: {}",
                    egmId, uid, tableId, session.getId());

        } catch (Exception e) {
            log.error("Error registering WebSocket session", e);
        }
    }

    public void remove(WebSocketSession session) {
        try {
            SessionMetadata metadata = sessions.remove(session.getId());

            if (metadata != null) {
                egmIdIndex.remove(metadata.getEgmId());
                uidIndex.remove(metadata.getUid());
                tableIdIndex.remove(metadata.getTableId());

                log.info("Removed WebSocket session - egmId: {}, uid: {}, tableId: {}, sessionId: {}",
                        metadata.getEgmId(), metadata.getUid(), metadata.getTableId(), session.getId());
            }
        } catch (Exception e) {
            log.error("Error removing WebSocket session", e);
        }
    }

    // Helper method to extract query parameters
    private String extractQueryParameter(WebSocketSession session, String paramName) {
        try {
            String uri = session.getUri().toString();
            String queryString = UriComponentsBuilder.fromUriString(uri)
                    .build()
                    .getQueryParams()
                    .getFirst(paramName);
            return queryString;
        } catch (Exception e) {
            log.error("Error extracting parameter: {}", paramName, e);
            return null;
        }
    }

    public WebSocketSession getSessionById(String sessionId) {
        SessionMetadata metadata = sessions.get(sessionId);
        return metadata != null ? metadata.getWebSocketSession() : null;
    }
    // Retrieve session by sessionId
    public WebSocketSession getSession(String sessionId) {
        SessionMetadata metadata = sessions.get(sessionId);
        return metadata != null ? metadata.getWebSocketSession() : null;
    }

    // Retrieve session by egmId
    public WebSocketSession getSessionByEgmId(String egmId) {
        String sessionId = egmIdIndex.get(egmId);
        return sessionId != null ? getSession(sessionId) : null;
    }

    // Retrieve session by uid
    public WebSocketSession getSessionByUid(String uid) {
        String sessionId = uidIndex.get(uid);
        return sessionId != null ? getSession(sessionId) : null;
    }

    // Retrieve session by tableId
    public WebSocketSession getSessionByTableId(String tableId) {
        String sessionId = tableIdIndex.get(tableId);
        return sessionId != null ? getSession(sessionId) : null;
    }

    // Get metadata for a session
    public SessionMetadata getMetadata(String sessionId) {
        return sessions.get(sessionId);
    }

    // Get all active sessions count
    public int getActiveSessionsCount() {
        return sessions.size();
    }

    // Check if session is registered
    public boolean isRegistered(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void broadcastToTable(String tableId, Object message) {
        for (SessionMetadata meta : sessions.values()) {
            WebSocketSession session = meta.getWebSocketSession();
            if (session == null || !session.isOpen()) continue;

            if (tableId.equals(meta.getTableId())) {
                send(session, message);
            }
        }
    }
    public void send(WebSocketSession session, Object message) {
        // This method can be implemented to send a message to a specific session.
        String jsonMessage = convertToJson(message);
        send(session, jsonMessage);
    }

    private void send(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToJson(Object message) {
        // Use Jackson ObjectMapper
        // to convert the message object to a JSON string.
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            // Handle JSON conversion exceptions
            e.printStackTrace();
            return "{}"; // Return empty JSON on error
        }

    }
}