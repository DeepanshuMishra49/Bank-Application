package com.banking.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Beneficiary entity representing saved payees for a customer's transfer convenience.
 */
@Entity
@Table(name = "beneficiaries", indexes = {
        @Index(name = "idx_beneficiaries_customer_id", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
