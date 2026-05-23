package com.terstredisproject1.infrastructure.client.ollama;

import com.terstredisproject1.infrastructure.client.AiAgentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OllamaAiAgentClient implements AiAgentClient {
    private final ChatClient chatClient;

    @Override
    public String ask(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
