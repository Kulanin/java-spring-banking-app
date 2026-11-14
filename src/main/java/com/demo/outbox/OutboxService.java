package com.demo.outbox;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.demo.events.TransactionCompletedEvent;
import com.demo.rabbitmq.RabbitMQConfig;
import com.demo.transaction.TransactionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void stageTransactionEvent(
            Long accountId,
            long amount,
            TransactionType type,
            String idempotencyKey,
            Long recordId,
            String routingKey) {

        TransactionCompletedEvent event = new TransactionCompletedEvent(
                accountId,
                BigDecimal.valueOf(amount),
                type,
                idempotencyKey,
                String.valueOf(recordId),
                Instant.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setExchange(RabbitMQConfig.TX_EXCHANGE);
        outboxEvent.setRoutingKey(routingKey);
        outboxEvent.setStatus("PENDING");
        outboxEvent.setCreatedAt(Instant.now());
        try {
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize " + type + " event", e);
        }

        outboxEventRepository.save(outboxEvent);
    }
}
