package com.yudhassif.election.repository;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, Long> {
    boolean existsByStatus(ElectionStatus electionStatus);

     List<Election> findByStatus(ElectionStatus electionStatus);
    Optional<Election> findFirstByStatus(ElectionStatus status);

    long countByStatus(ElectionStatus electionStatus);
    // Optimized: Database handles the time comparison in one go
    @Modifying
    @Query("UPDATE Election e SET e.status = 'ACTIVE' " +
            "WHERE e.status = 'PENDING' AND e.votingStartTime <= :now")
    int activatePendingElections(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE Election e SET e.status = 'CLOSED' " +
            "WHERE e.status = 'ACTIVE' AND e.votingEndTime <= :now")
    int closeActiveElections(@Param("now") Instant now);

}
