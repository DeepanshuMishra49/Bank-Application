package com.banking.entity;

import com.banking.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a security role in the system (ADMIN, EMPLOYEE, CUSTOMER).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleName name;
}
