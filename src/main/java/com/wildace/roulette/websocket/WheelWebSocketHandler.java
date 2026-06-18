package com.wildace.roulette.websocket;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildace.roulette.events.PlaceBetMessage;
import com.wildace.roulette.events.SessionConnectedEvent;
import com.wildace.roulette.services.InMemoryBetStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
@Slf4j
public class WheelWebSocketHandler extends TextWebSocketHandler {
    // This class can be expanded to handle messages related to the roulette wheel,
    // such as broadcasting spin results to connected clients.
    private final ApplicationEventPublisher publisher;
    private final GatewayMessageRouter messageRouter;
    private final ConnectionRegistry registry;


    public WheelWebSocketHandler(ApplicationEventPublisher eventPublisher, GatewayMessageRouter messageRouter, ConnectionRegistry registry) {
        this.publisher = eventPublisher;
        this.messageRouter = messageRouter;
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();

        if(uri == null || !uri.getPath().equals("/websocket/game")) {
            log.warn("Invalid WebSocket connection attempt: {}", session.getId());
            session.close();
        } else {
            log.info("Valid WebSocket connection attempt: {}", session.getId());
            Map<String, String> queryParams = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();

            String egmId = queryParams.get("egmId");
            String uid = queryParams.get("uid");
            String tableId = queryParams.get("tableId");

            // Validate parameters
            if (egmId == null || uid == null) {
                session.close(CloseStatus.BAD_DATA.withReason("Missing egmId or uid parameter"));
                return;
            }

            // Store in session attributes for later use
            session.getAttributes().put("egmId", egmId);
            session.getAttributes().put("uid", uid);
            session.getAttributes().put("tableId", tableId != null ? tableId : "1");

            registry.register(session);
            log.info("WebSocket connection established: {}, EGM: {}, UID: {}, Table: {}",
                    session.getId(), egmId, uid, tableId != null ? tableId : "1");

            var event = new SessionConnectedEvent(session.getId(), uid, egmId, tableId != null ? tableId : "1");
            publisher.publishEvent(event);

        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        registry.remove(session);
        log.info("WebSocket connection closed: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageRouter.route(session, message);
    }

}

