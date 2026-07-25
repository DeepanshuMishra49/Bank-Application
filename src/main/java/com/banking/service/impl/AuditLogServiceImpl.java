package com.banking.service.impl;

import com.banking.entity.AuditLog;
import com.banking.repository.AuditLogRepository;
import com.banking.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asynchronous implementation of {@link AuditLogService} persisting events to the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional
    public void log(String action, String performedBy, String entityType, String entityId,
                    String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .success(true)
                .build();
        auditLogRepository.save(auditLog);
        log.debug("Audit [{}] by {} on {}:{}", action, performedBy, entityType, entityId);
    }

    @Override
    @Async
    @Transactional
    public void logFailure(String action, String performedBy, String entityType, String entityId,
                           String errorMessage, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .entityType(entityType)
                .entityId(entityId)
                .details("FAILED: " + errorMessage)
                .ipAddress(ipAddress)
                .success(false)
                .errorMessage(errorMessage)
                .build();
        auditLogRepository.save(auditLog);
        log.warn("Audit FAILURE [{}] by {} - {}", action, performedBy, errorMessage);
    }
}
