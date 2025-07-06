package com.talk.katalkclone.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talk.katalkclone.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 사용자 ID → 세션
    // 사용자가 많아지면 메모리 증가
    // 그리고 분산 서버로 확장 시 세션 추적의 어려움 추후에 리팩토링 필요
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromQuery(session);
        sessions.put(userId, session);
        log.info("Session {} connected", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromQuery(session);
        sessions.remove(userId);
        System.out.println("❌ 사용자 연결 종료: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("받은 메시지: {}", payload);

        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

        WebSocketSession receiverSession = sessions.get(chatMessage.getReceiverId());
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(payload));
        }

    }

    private String getUserIdFromQuery(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return "unknown";
        String query = uri.getQuery(); // 예: userId=userA
        return query != null && query.startsWith("userId=")
                ? query.split("=")[1]
                : "unknown";
    }
}
