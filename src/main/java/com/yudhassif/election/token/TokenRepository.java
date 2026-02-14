package com.yudhassif.election.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
    SELECT t FROM Token t
    JOIN t.user u
    WHERE u.id = :userId
    AND t.expired = false
    AND t.revoked = false
    """)
    List<Token> findAllValidTokensByUser(long userId);

    boolean existsByTokenHashAndExpiredFalseAndRevokedFalse(String tokenHash);

    Optional<Token> findByTokenHash(String tokenHash);
}




































//package com.yudhassif.election.token;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import java.util.List;
//import java.util.Optional;
//
//public interface TokenRepository extends JpaRepository<Token, Long> {
//    @Query("""
//SELECT t FROM Token t
//JOIN t.user u
//WHERE u.id = :userId
//AND t.expired = false
//AND t.revoked = false
//""")
//    List<Token> findAllValidTokensByUser(long userId); //the error of nt joinable to resolve the problem was on token repository change t.token into t.user
//
////    Optional<Token> findByResetToken(String resetToken);
//
//    @Modifying
//    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
//    void invalidateAllForUser(long userId);
//
//
//    boolean existsByTokenHashAndExpiredFalseAndRevokedFalse(String hashedToken);
//
//    Optional<Token> findByTokenHash(String hashedToken);
//}
//
