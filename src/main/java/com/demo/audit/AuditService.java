package com.demo.audit;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
public class AuditService {

    final private AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;

    }

    public boolean isAlreadyProcessed(String eventId) {
        return auditRepository.existsByEventId(eventId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String eventId, String username, String action, String details) {

        if (auditRepository.existsByEventId(eventId)) {
            return; // already processed
        }
        AuditLog log = new AuditLog();
        log.setEventId(eventId);
        log.setUsername(username);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());

        auditRepository.save(log);

    }

}
