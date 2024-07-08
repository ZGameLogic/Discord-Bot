package com.zgamelogic.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class PlannerWebsocketService extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("Connected: {}", session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received: {}", payload);
        session.sendMessage(new TextMessage("Echo: " + payload));
        sendMessage("BEP");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        log.debug("Disconnected: {}", session.getId());
        sessions.remove(session);
    }

    public void sendMessage(Object message){
        ObjectMapper om = new ObjectMapper();
        try {
            String output = om.writeValueAsString(message);
            for(WebSocketSession session: sessions) session.sendMessage(new TextMessage(output));
        } catch (JsonProcessingException e) {
            log.error("Unable to create JSON object", e);
        } catch (IOException i){
            log.error("Unable to send message", i);
        }
    }
}
