package com.yudhassif.election.repository;

import com.yudhassif.election.entity.PasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public interface PasswordOtpRepository extends JpaRepository<PasswordOtp, Long> {
    List<PasswordOtp> findByUserIdOrderByExpiresAtDesc(long userId);
    void deleteByExpiresAtBefore(Instant cutoff);
    @Query("""
 SELECT o FROM PasswordOtp o
 WHERE o.user.id = :userId
   AND o.used = false
   AND o.expiresAt > CURRENT_TIMESTAMP
 ORDER BY o.createdAt DESC
""")
    Optional<PasswordOtp> findLatestOtpForUser(long userId);

}

