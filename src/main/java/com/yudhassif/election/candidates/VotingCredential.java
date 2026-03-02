package com.yudhassif.election.candidates;

import com.yudhassif.election.entity.CredentialStatus;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "voting_credentials",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_student_election_credential",
                        columnNames = {"student_id", "election_id", "status"}
                )
        }
)
//@Table(name = "voting_credentials",
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"voterId"}),
//        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotingCredential { // voting credentials

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String voterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Election election;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialStatus status;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    private String ipAddress;
    private String deviceInfo;
    private boolean isUsed;

    @Version
    private Long version;
}
































//package com.yudhassif.election.candidates;
//
//import com.yudhassif.election.entity.Election;
//import com.yudhassif.election.entity.Student;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(
//        name = "votes",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        columnNames = {"student_id", "position_id", "election_id"}
//                )
//        }
//)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Vote {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Who voted
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "student_id", nullable = false)
//    private Student student;
//
//    // What election
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "election_id", nullable = false)
//    private Election election;
//
//    // Which position
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "position_id", nullable = false)
//    private Position position;
//
//    // Which leader (candidate)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "leader_id", nullable = false)
//    private Leader leader;
//}
