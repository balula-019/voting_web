package com.yudhassif.election.repository;

import com.yudhassif.election.candidates.VotingCredential;
import com.yudhassif.election.entity.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<VotingSession , Long> {

    Optional<VotingSession> findByCredentialAndActiveTrue(VotingCredential credential);
}
