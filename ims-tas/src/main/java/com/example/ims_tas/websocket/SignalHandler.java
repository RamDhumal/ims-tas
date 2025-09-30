package com.example.ims_tas.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

public class SignalHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> users = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = mapper.readTree(message.getPayload());
        String type = node.get("type").asText();
        String user = node.has("user") ? node.get("user").asText() : null;
        String to = node.has("to") ? node.get("to").asText() : null;

        switch (type) {
            case "REGISTER":
                if (user != null) users.put(user, session);
                session.sendMessage(new TextMessage("{\"type\":\"REGISTERED\"}"));
                break;

            case "START_CALL":
                WebSocketSession callee = users.get(to);
                if (callee != null) callee.sendMessage(new TextMessage("{\"type\":\"INCOMING_CALL\",\"from\":\"" + user + "\"}"));
                break;

            case "OFFER":
            case "ANSWER":
            case "ICE_CANDIDATE":
                WebSocketSession peer = users.get(to);
                if (peer != null) peer.sendMessage(new TextMessage(message.getPayload()));
                break;

            case "END_CALL":
                WebSocketSession other = users.get(to);
                if (other != null) other.sendMessage(new TextMessage("{\"type\":\"CALL_ENDED\"}"));
                break;
        }
    }
}
