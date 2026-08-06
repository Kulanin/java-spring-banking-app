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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String username, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());

        auditRepository.save(log);

    }

}
