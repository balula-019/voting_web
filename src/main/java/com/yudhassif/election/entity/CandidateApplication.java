package com.yudhassif.election.entity;


import com.yudhassif.election.candidates.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student who applied
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    // Position applied for
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    private Position position;

    // Election reference (denormalization for easier queries)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "election_id")
    private Election election;

    // GPA submitted during application
    @Column(nullable = false)
    private Double gpa;

    // Student campaign statement
    @Column(length = 2000)
    private String manifesto; // student policy

    // Campaign image uploaded by student
    private String campaignImageUrl;

    // Application status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // Admin rejection reason (optional)
    private String rejectionReason;

    // Audit fields
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    @Column(nullable = false)
    private Long reviewedBy;

    public void approve(Long adminId) {
        this.status = ApplicationStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = adminId;
    }

    public void reject(Long adminId, String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = adminId;
        this.rejectionReason = reason;
    }

}

//
//import com.yudhassif.election.candidates.Position;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "candidate_applications",
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"student_id", "election_id"})
//        })
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CandidateApplication {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    private Student student;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    private Election election;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    private Position position;
//
//    @Column(length = 2000)
//    private String manifesto;
//
//    @Enumerated(EnumType.STRING)
//    private ApplicationStatus status;
//
//    private Double gpa;
//
//    private String campaignPhotoUrl;
//
//    private Long approvedByAdminId;
//
//    private LocalDateTime approvedAt;
//
//    private LocalDateTime createdAt;
//}

