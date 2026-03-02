package com.yudhassif.election.candidates;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.Fetch;

import java.time.Instant;

@Entity
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"student_id", "election_id", "position_id"}
                )
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    private Leader candidate; // leader voted for

    private Instant votedAt;
}