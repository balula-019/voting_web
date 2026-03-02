package com.yudhassif.election.services;

import com.yudhassif.election.candidates.Leader;
import com.yudhassif.election.candidates.Vote;
import com.yudhassif.election.candidates.VotingCredential;
import com.yudhassif.election.entity.ApplicationStatus;
import com.yudhassif.election.entity.CredentialStatus;
import com.yudhassif.election.entity.Student;
import com.yudhassif.election.repository.LeaderRepository;
import com.yudhassif.election.repository.VoteRepository;
import com.yudhassif.election.repository.VotingCredentialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class voteService{
    private final StudentService studentService;
    private final Clock clock;
    private final LeaderRepository leaderRepository;
    private final VotingCredentialRepository votingCredentialRepository;
    private final VoteRepository voteRepository;

    @Transactional
    public void castVote(
            Long electionId,
            Long credentialId,
            Long  leaderId
    ) {

        Student student = studentService.getAuthenticatedStudent().getStudent();

        VotingCredential credential = votingCredentialRepository
                .findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        if (!credential.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("Credential does not belong to you");
        }

        if (!credential.getElection().getId().equals(electionId)) {
            throw new IllegalStateException("Invalid election");
        }

        if (credential.getStatus() != CredentialStatus.GENERATED) {
            throw new IllegalStateException("Credential already used or expired");
        }

        if (credential.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new IllegalStateException("Credential expired");
        }

        Leader candidate = leaderRepository
                .findById(leaderId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (!candidate.getElection().getId().equals(electionId)) {
            throw new IllegalStateException("Candidate not in this election");
        }

        if (candidate.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Candidate not approved");
        }

        // Duplicate protection
        if (voteRepository.existsByStudentAndElectionAndPosition(
                student,
                candidate.getElection(),
                candidate.getPosition())) {
            throw new IllegalStateException("Already voted for this position");
        }

        Vote vote = Vote.builder()
                .student(student)
                .election(candidate.getElection())
                .position(candidate.getPosition())
                .candidate(candidate)
                .votedAt(Instant.now(clock))
                .build();

        voteRepository.save(vote);

        credential.setStatus(CredentialStatus.USED);
        credential.setUsedAt(Instant.now(clock));
    }
}
// todo to test for student picking the vote
// todo results to be calculated in the system
// todo to deals with the chart in admin dashboard and return total student who vote in the system in admin dashboard
