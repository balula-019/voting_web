package com.yudhassif.election.repository;

import com.yudhassif.election.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);

    Optional<PasswordResetToken> findByResetTokenAndUsedFalse(String rawToken);

    // ✅ Mark all unused tokens of a user as used
    @Modifying
    @Transactional
    @Query("""
        UPDATE PasswordResetToken t
        SET t.used = true
        WHERE t.user.id = :userId
        AND t.used = false
    """)
    void invalidateAllForUser(long userId);
}
