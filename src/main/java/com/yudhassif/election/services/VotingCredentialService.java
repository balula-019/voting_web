package com.yudhassif.election.services;

import com.yudhassif.election.candidates.VotingCredential;
import com.yudhassif.election.entity.*;
import com.yudhassif.election.generator.VoterIdGenerator;
import com.yudhassif.election.repository.ElectionRepository;
import com.yudhassif.election.repository.SessionRepository;
import com.yudhassif.election.repository.VotingCredentialRepository;
import com.yudhassif.election.response.ActivateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VotingCredentialService {

    private final VotingCredentialRepository repository;
    private final VoterIdGenerator generator;
    private final Clock clock;
    private final ElectionRepository electionRepository;
    private final ElectionService electionService;
    private final SessionRepository sessionRepository;

    private static final int EXPIRATION_MINUTES = 5;

    /*
     MAIN ENTRY POINT
     */
    @Transactional
    public String generateCredential(Student student,
                                     Election election,
                                     String ip,
                                     String device) {

        // 1 ensure election active
//        if (!election.isActive()) {
//            throw new RuntimeException("Election is not active");
//        }
        if (!electionRepository.existsByStatus(ElectionStatus.ACTIVE)){
            throw new IllegalArgumentException("Election is not in active mode");

        }

        // 2 check if already voted (USED credential exists)
        boolean alreadyVoted = repository.existsByStudentAndElectionAndStatus(
                student,
                election,
                CredentialStatus.USED
        );

        if (alreadyVoted) {
            throw new RuntimeException("Student already voted in this election");
        }

        // 3 check existing ACTIVE credential
        var existing = repository.findByStudentAndElectionAndStatus(
                student,
                election,
                CredentialStatus.GENERATED
        );

        if (existing.isPresent()) {

            VotingCredential cred = existing.get();

            if (isExpired(cred)) {
                expireCredential(cred);
            } else {
                return cred.getVoterId(); // reuse existing
            }
        }
        Instant now = Instant.now(clock);
        // 4 generate new credential
        String voterId = generateUniqueVoterId();

        VotingCredential credential = VotingCredential.builder()
                .voterId(voterId)
                .student(student)
                .election(election)
                .status(CredentialStatus.GENERATED)
                .issuedAt(now)
                .expiresAt(now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                .ipAddress(ip)
                .deviceInfo(device)
                .build();

        repository.save(credential);

        return voterId;
    }

    /*
     UNIQUE GENERATOR WITH COLLISION PROTECTION
     */
    private String generateUniqueVoterId() {
        String id;
        do {
            id = generator.generate();
        } while (repository.findByVoterId(id).isPresent());
        return id;
    }

    /*
     EXPIRATION CHECK
     */
    public boolean isExpired(VotingCredential cred) {
        return Instant.now().isAfter(cred.getExpiresAt());
    }

    /*
     MARK EXPIRED
     */
    @Transactional
    public void expireCredential(VotingCredential cred) {
        cred.setStatus(CredentialStatus.EXPIRED);
        repository.save(cred);
    }
//
//    public ActivateResponse activate(Long electionId, String s) {
//    }
@Transactional
public ActivateResponse activate(Long electionId, String voterId) {

    Election election = electionService.getActiveElectionById(electionId);

    VotingCredential credential =
            repository.findByVoterIdAndElectionId(voterId, electionId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Invalid voter ID"));

    if (credential.isUsed()) {
        throw new IllegalStateException("Voter ID already used");
    }

    Optional<VotingSession> existingOpt =
            sessionRepository.findByCredentialAndActiveTrue(credential);

    if (existingOpt.isPresent()) {

        VotingSession existing = existingOpt.get();

        if (Instant.now().isAfter(existing.getExpiresAt())) {
            // session expired → deactivate
            existing.setActive(false);
        } else {
            throw new IllegalStateException("Session already active");
        }
    }

    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(600);

    VotingSession newSession = VotingSession.builder()
            .token(UUID.randomUUID().toString())
            .credential(credential)
            .startedAt(now)
            .expiresAt(expiry)
            .active(true)
            .build();

    sessionRepository.save(newSession);

    return new ActivateResponse(newSession.getToken(), expiry);
}
    // todo
}
