package com.yudhassif.election.token;

import com.yudhassif.election.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;


public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

    /**
     * Find an activation token by its hashed value.
     * This is safer than fetching all and filtering in memory.
     */
//    Optional<ActivationToken> findByTokenHash(String tokenHash);

    /**
     * Optional: find all unused and unexpired activation tokens for a student
     */
//    List<ActivationToken> findByStudentIdAndUsedFalse(Long studentId);

    Optional<ActivationToken> findByToken( String token);

    Optional<ActivationToken> findAllByUsedFalseAndExpiresAtAfter(Instant now);

    Optional<ActivationToken> findByUserAndUsedFalse(User user);

    @Modifying
    @Query("""
       update ActivationToken t
       set t.used = true
       where t.user = :user and t.used = false
       """)
    void invalidateAllActiveTokens(User user);

}
