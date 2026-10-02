package com.demo.outbox;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class OutboxEvent {
    @Id
    @GeneratedValue
    private Long id;
    private String exchange;
    private String routingKey;
    private String payload; // JSON
    private String status; // PENDING, PUBLISHED, FAILED
    private Instant createdAt;
    private Instant publishedAt;

}