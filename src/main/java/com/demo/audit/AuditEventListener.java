package com.demo.audit;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.demo.events.WithdrawalCompletedEvent;
import com.demo.rabbitmq.RabbitMQConfig;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onWithdrawalCompleted(
            WithdrawalCompletedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try {
            if (auditService.isAlreadyProcessed(event.idempotencyKey())) {
                log.info("Event {} already processed, skipping", event.idempotencyKey());
                channel.basicAck(tag, false);
                return;
            }

            auditService.logAction(
                    event.idempotencyKey(),
                    "system",
                    "WITHDRAWAL",
                    "Withdrew " + event.amount() + " from account " + event.accountId());

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Audit processing failed for delivery tag {}", tag, e);
            channel.basicNack(tag, false, false);
        }
    }
}