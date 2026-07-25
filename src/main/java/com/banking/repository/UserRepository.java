package com.banking.repository;

import com.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entity with custom authentication-related queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = :attempts WHERE u.email = :email")
    void updateFailedAttempts(@Param("attempts") int attempts, @Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = CURRENT_TIMESTAMP WHERE u.email = :email")
    void lockAccount(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true, u.lockTime = null, u.failedAttempts = 0 WHERE u.email = :email")
    void unlockAccount(@Param("email") String email);
}
