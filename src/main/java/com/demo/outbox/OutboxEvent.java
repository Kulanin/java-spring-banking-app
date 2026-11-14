package com.demo.outbox;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_outbox_status_created_at", columnList = "status, created_at")
})
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON

    @Column(nullable = false, length = 20)
    private String status; // PENDING, PUBLISHED, FAILED

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private Instant publishedAt;
}
