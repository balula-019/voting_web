package com.yudhassif.election.repository;

import com.yudhassif.election.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);
    Optional<User> findById(long userId);

    boolean existsByRole_Name(String admin);

    @Query("""
       SELECT u
       FROM User u
       LEFT JOIN u.student s
       WHERE u.email = :identifier
          OR s.mail = :identifier
       """)
    Optional<User> findByIdentifier(String identifier);
}
