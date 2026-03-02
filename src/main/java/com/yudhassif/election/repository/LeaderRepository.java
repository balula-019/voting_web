package com.yudhassif.election.repository;

import com.yudhassif.election.candidates.Leader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaderRepository extends JpaRepository<Leader, Long> {
//    Optional<Leader> findByLeaderId(Long leaderId);
}
