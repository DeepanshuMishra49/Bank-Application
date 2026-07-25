package com.banking.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Audit log entity capturing all significant system events for compliance and security.
 * Records who did what, when, from where, and to which entity.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private boolean success = true;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
