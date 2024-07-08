package com.zgamelogic.configurations;

import com.zgamelogic.services.PlannerWebsocketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfiguration implements WebSocketConfigurer {
    private final PlannerWebsocketService plannerWebsocketService;

    public WebsocketConfiguration(PlannerWebsocketService plannerWebsocketService) {
        this.plannerWebsocketService = plannerWebsocketService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(plannerWebsocketService, "/planner").setAllowedOrigins("*");
    }
}
