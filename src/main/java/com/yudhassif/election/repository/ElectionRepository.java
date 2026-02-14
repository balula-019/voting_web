package com.yudhassif.election.repository;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, Long> {
    boolean existsByStatus(ElectionStatus electionStatus);

     List<Election> findByStatus(ElectionStatus electionStatus);
    Optional<Election> findFirstByStatus(ElectionStatus status);

    long countByStatus(ElectionStatus electionStatus);
}
