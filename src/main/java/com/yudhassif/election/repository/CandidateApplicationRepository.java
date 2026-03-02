package com.yudhassif.election.repository;

import com.yudhassif.election.entity.ApplicationStatus;
import com.yudhassif.election.entity.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateApplicationRepository
        extends JpaRepository<CandidateApplication, Long> {

    Optional<CandidateApplication> findByStudentIdAndElectionId(Long studentId, Long electionId);

    List<CandidateApplication> findByElectionIdAndStatus(Long electionId, ApplicationStatus status);

    boolean existsByStudentIdAndElectionId(Long studentId, Long electionId);

    boolean existsByStudentIdAndPositionId(long id, Long positionId);
    int countApprovedByPositionId(Long id);

    Optional<CandidateApplication> findByElectionIdAndStudentId(Long electionId, Long studentId);
}

