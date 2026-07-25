package com.banking.service;

import com.banking.entity.AuditLog;

/**
 * Service interface for recording system audit events.
 */
public interface AuditLogService {

    /**
     * Records a successful audit event.
     *
     * @param action      the action performed
     * @param performedBy the username who performed it
     * @param entityType  the entity type affected
     * @param entityId    the entity ID affected
     * @param details     additional details
     * @param ipAddress   the requester's IP
     */
    void log(String action, String performedBy, String entityType, String entityId,
             String details, String ipAddress);

    /**
     * Records a failed audit event with error information.
     *
     * @param action       the action attempted
     * @param performedBy  the username who attempted it
     * @param entityType   the entity type involved
     * @param entityId     the entity ID involved
     * @param errorMessage the failure reason
     * @param ipAddress    the requester's IP
     */
    void logFailure(String action, String performedBy, String entityType, String entityId,
                    String errorMessage, String ipAddress);
}
