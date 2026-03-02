package com.yudhassif.election.candidates;

import com.yudhassif.election.entity.ApplicationStatus;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // candidate info snapshot (optional but useful)

    private String imageUrl;

    private Double gpa;

    @Column(nullable = false)
    private boolean active = true;
    @Column(length = 2000)
    private String manifesto;

    private LocalDateTime approvedAt;

    // 🔹 link to student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // 🔹 position they are contesting
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    // 🔹 election they belong to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;
}
