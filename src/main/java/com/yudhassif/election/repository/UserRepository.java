package com.yudhassif.election.repository;

import com.yudhassif.election.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);
    Optional<User> findById(long userId);

    boolean existsByRole_Name(String admin);
}
