package com.yudhassif.election.repository;

import com.yudhassif.election.candidates.Position;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position,Long> {
    Optional<Position> findById(Long positionId);

    boolean existsByNameAndElectionId(String name, Long electionId);

}
