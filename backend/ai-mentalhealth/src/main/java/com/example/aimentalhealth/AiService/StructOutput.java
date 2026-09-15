package com.example.aimentalhealth.AiService;

public class StructOutput {
    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            Integer messageCount,
            String status
    ){

    }
}
