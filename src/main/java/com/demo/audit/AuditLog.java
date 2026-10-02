package com.demo.audit;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs", uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)

    private String eventId;
    private String username;
    private String action;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress;

}
