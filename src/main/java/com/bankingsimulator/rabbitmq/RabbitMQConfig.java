package com.bankingsimulator.rabbitmq;

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

    public static final String TX_EXCHANGE = "banking.tx.exchange";
    public static final String DLX_EXCHANGE = "banking.dlx.exchange";

    public static final String AUDIT_QUEUE = "banking.audit.queue";
    public static final String NOTIFICATION_QUEUE = "banking.notification.queue";
    public static final String AUDIT_DLQ = "banking.audit.dlq";

    public static final String TX_WITHDRAWAL_KEY = "tx.withdrawal.completed";
    public static final String TX_DEPOSIT_KEY = "tx.deposit.completed";
    public static final String TX_TRANSFER_OUT_KEY = "tx.transfer.out.completed";
    public static final String TX_TRANSFER_IN_KEY = "tx.transfer.in.completed";

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

    @Bean
    public TopicExchange txExchange() {
        return new TopicExchange(TX_EXCHANGE);
    }

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
                .with("tx.#.completed");
    }

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
                .with("tx.withdrawal.completed");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}