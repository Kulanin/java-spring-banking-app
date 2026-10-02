package com.demo.outbox;

import java.time.Instant;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.events.WithdrawalCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Service
@Data
public class OutboxPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepo.findPendingEvents(PageRequest.of(0, 100));
        for (OutboxEvent event : events) {
            try {

                WithdrawalCompletedEvent withdrawalEvent = objectMapper.readValue(
                        event.getPayload(),
                        WithdrawalCompletedEvent.class);
                rabbitTemplate.convertAndSend(
                        event.getExchange(),
                        event.getRoutingKey(),
                        withdrawalEvent);
                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                event.setStatus("FAILED"); // Will retry on next poll
            }
        }
    }
}
