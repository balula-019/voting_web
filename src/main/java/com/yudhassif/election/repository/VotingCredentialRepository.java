package com.yudhassif.election.repository;


import com.yudhassif.election.candidates.VotingCredential;
import com.yudhassif.election.entity.CredentialStatus;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VotingCredentialRepository extends JpaRepository<VotingCredential, Long> {

    Optional<VotingCredential> findByVoterId(String voterId);

    Optional<VotingCredential> findByStudentAndElectionAndStatus(
            Student student,
            Election election,
            CredentialStatus status
    );

    boolean existsByStudentAndElectionAndStatus(
            Student student,
            Election election,
            CredentialStatus status
    );

    boolean existsByStudentAndElectionAndStatusIn(
            Student student,
            Election election,
            java.util.List<CredentialStatus> statuses
    );

    void deleteByExpiresAtBeforeAndStatus(LocalDateTime time, CredentialStatus status);

    Optional<VotingCredential> findByVoterIdAndElectionId(String voterId, Long electionId);
}
