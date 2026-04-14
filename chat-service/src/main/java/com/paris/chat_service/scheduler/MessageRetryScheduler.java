package com.paris.chat_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paris.chat_service.model.Message;
import com.paris.chat_service.service.ChatService;
import com.paris.chat_service.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageRetryScheduler {

    private final ChatService chatService;
    private final SessionRegistry sessionRegistry;
    private final ObjectMapper mapper;

    private static final int MAX_RETRIES = 3;

    @Scheduled(fixedDelay = 5000) // every 5 sec
    public void retryPendingMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(10);

        List<Message> messages = chatService.findMessagesForRetry(threshold);

        for (Message msg : messages) {
            try {
                if (msg.getRetryCount() >= MAX_RETRIES) {
                    log.warn("Message {} moved to DLQ", msg.getId());
                    chatService.moveToDLQ(msg);
                    continue;
                }

                if (sessionRegistry.isOnline(msg.getReceiverId())) {

                    sessionRegistry.sendToUser(
                            msg.getReceiverId(),
                            mapper.writeValueAsString(msg)
                    );

                    chatService.incrementRetry(msg.getId());

                    log.info("Retry sent for message {}", msg.getId());
                }

            } catch (Exception e) {
                log.error("Retry failed for message {}", msg.getId(), e);
            }
        }
    }
}
