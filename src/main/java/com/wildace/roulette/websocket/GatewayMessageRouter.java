package com.wildace.roulette.websocket;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildace.roulette.events.SessionErrorEvent;
import com.wildace.roulette.services.TerminalSessionApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Routes incoming WebSocket messages to appropriate handlers.
 * Publishes events for each message type to enable event-driven architecture.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayMessageRouter {

    private final TerminalSessionApplicationService appService;
    private final ConnectionRegistry registry;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Route GameMessage based on type and publish events
     */
    public void route(WebSocketSession session, GameMessage gameMessage) {
        try {
            ConnectionRegistry.SessionMetadata metadata = registry.getMetadata(session.getId());

            switch (gameMessage.type()) {
                case PLACE_BET -> {
                    appService.handlePlaceBet(session, gameMessage);
                }
                case HEARTBEAT -> {
                    appService.handleHeartbeat(session);
                }

                default -> {
                    String errorMsg = "Unknown message type: " + gameMessage.type();
                    log.error(errorMsg);
                }
            }
        } catch (Exception e) {
            log.error("Error routing message of type: {}", gameMessage.type(), e);
            try {
                eventPublisher.publishEvent(new SessionErrorEvent(session.getId(), "Error processing message: " + e.getMessage()));
            } catch (Exception eventError) {
                log.error("Error publishing error event", eventError);
            }
        }
    }

    /**
     * Route TextMessage by deserializing to GameMessage first
     */
    public void route(WebSocketSession session, TextMessage textMessage) {
        try {
            // Deserialize TextMessage payload to GameMessage
            String payload = textMessage.getPayload();
            GameMessage gameMessage = objectMapper.readValue(payload, GameMessage.class);

            // Route the deserialized message
            route(session, gameMessage);

        } catch (JsonMappingException e) {
            log.error("Invalid JSON format in message", e);
            try {
                sendErrorResponse(session, "Invalid message format");
                eventPublisher.publishEvent(new SessionErrorEvent(session.getId(), "Invalid message format: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("Error sending error response", ex);
            }
        } catch (IllegalArgumentException e) {
            log.error("Unknown message type", e);
            try {
                sendErrorResponse(session, e.getMessage());
                eventPublisher.publishEvent(new SessionErrorEvent(session.getId(), "Unknown message type: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("Error sending error response", ex);
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            try {
                sendErrorResponse(session, "Internal server error");
                eventPublisher.publishEvent(new SessionErrorEvent(session.getId(), "Internal server error: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("Error sending error response", ex);
            }
        }
    }

    /**
     * Send error response to client
     */
    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        if (session != null && session.isOpen()) {
            ErrorResponse error = new ErrorResponse(errorMessage);
            String json = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(json));
            log.debug("Sent error response to session: {}", session.getId());
        }
    }

}