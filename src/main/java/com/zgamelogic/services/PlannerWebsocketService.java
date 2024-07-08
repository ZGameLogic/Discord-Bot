package com.zgamelogic.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zgamelogic.data.authData.DiscordUser;
import com.zgamelogic.data.database.authData.AuthData;
import com.zgamelogic.data.database.authData.AuthDataRepository;
import com.zgamelogic.data.database.planData.plan.Plan;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PlannerWebsocketService extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions;
    private final AuthDataRepository authDataRepository;
    private final DiscordService discordService;

    public PlannerWebsocketService(AuthDataRepository authDataRepository, DiscordService discordService) {
        this.authDataRepository = authDataRepository;
        this.discordService = discordService;
        sessions = Collections.synchronizedSet(new HashSet<>());
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        log.debug("Connected: {}", session.getId());
        String device = session.getHandshakeHeaders().getFirst("device");
        String token = session.getHandshakeHeaders().getFirst("token");

        if (token == null || device == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        Optional<DiscordUser> discordUser = discordService.getUserFromToken(token);
        if (discordUser.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        Optional<AuthData> authData = authDataRepository.findById_DiscordIdAndId_DeviceIdAndToken(discordUser.get().id(), device, token);
        if (authData.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("Received: {}", payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        log.debug("Disconnected: {}", session.getId());
        sessions.remove(session);
    }

    public void sendMessage(Object message){
        Plan.PlanSerialization planSerialization = new Plan.PlanSerialization();
        ObjectMapper om = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Plan.class, planSerialization);
        om.registerModule(module);
        try {
            System.out.println(new String(om.writeValueAsBytes(message)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            for(WebSocketSession session: sessions) session.sendMessage(new BinaryMessage(om.writeValueAsBytes(message)));
        } catch (JsonProcessingException e) {
            log.error("Unable to create JSON object", e);
        } catch (IOException i){
            log.error("Unable to send message", i);
        }
    }
}
