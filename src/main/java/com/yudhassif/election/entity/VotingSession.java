package com.yudhassif.election.entity;

import com.yudhassif.election.candidates.VotingCredential;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@Table(name = "voting_session")
public class VotingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private VotingCredential credential;

    private Instant startedAt;

    private Instant expiresAt;

    private boolean active;
}
