package com.demo.rabbitmq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.core.AcknowledgeMode;

@Configuration
public class RabbitMQConfig {

    // === EXCHANGES ===
    public static final String TX_EXCHANGE = "banking.tx.exchange";
    public static final String DLX_EXCHANGE = "banking.dlx.exchange";

    // === QUEUES ===
    public static final String AUDIT_QUEUE = "banking.audit.queue";
    public static final String NOTIFICATION_QUEUE = "banking.notification.queue";
    public static final String AUDIT_DLQ = "banking.audit.dlq";

    // === ROUTING KEYS ===
    public static final String TX_WITHDRAWAL_KEY = "tx.withdrawal.completed";
    public static final String TX_DEPOSIT_KEY = "tx.deposit.completed";

    // --- Dead Letter Infrastructure (shared across all services) ---
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(AUDIT_DLQ).build();
    }

    @Bean
    public Binding auditDlqBinding() {
        return BindingBuilder.bind(auditDlq()).to(dlxExchange()).with("audit.failed");
    }

    // --- Main Exchange ---
    @Bean
    public TopicExchange txExchange() {
        return new TopicExchange(TX_EXCHANGE);
    }

    // --- Audit Queue with DLX configured ---
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "audit.failed")
                .withArgument("x-queue-type", "quorum") // High availability
                .build();
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue())
                .to(txExchange())
                .with("tx.#.completed"); // Topic wildcard: all completed transactions
    }

    // --- Notification Queue ---
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-queue-type", "quorum")
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(txExchange())
                .with("tx.withdrawal.completed"); // Only withdrawals trigger notifications
    }

    // --- JSON Message Converter (critical for type safety) ---
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- Listener Container Factory (shared by ALL @RabbitListeners) ---
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(3); // Start with 3 consumers
        factory.setMaxConcurrentConsumers(10); // Scale up to 10 under load
        factory.setPrefetchCount(10); // Fair dispatch: max 10 unacked per consumer
        factory.setDefaultRequeueRejected(false); // Don't requeue business failures (send to DLQ)
        return factory;
    }
}