package com.yudhassif.election.entity;


import com.yudhassif.election.candidates.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "elections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Election {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String electionName;

    private String description;

    private String academicYear;
    @Enumerated(EnumType.STRING)
    private ElectionStatus status;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    private Instant votingStartTime;

    private Instant votingEndTime;
//added application window time for student
    private Instant applicationStartTime;

    private Instant applicationEndTime;

    @OneToMany(mappedBy = "election",fetch = FetchType.LAZY)
    private List<Position> positions;

    public boolean isApplicationOpen() {
        Instant now = Instant.now();
        return now.isAfter(applicationStartTime)
                && now.isBefore(applicationEndTime);
    }

    public boolean isApplicationClosed() {
        return Instant.now().isAfter(applicationEndTime);
    }

    public boolean isActive() {

        Instant now = Instant.now();

        return votingStartTime != null
                && votingEndTime != null
                && !now.isBefore(votingStartTime)
                && !now.isAfter(votingEndTime);
    }
}
// vote is the main action so so we just ignored the relationship with vote because vote will be calculated in the database
