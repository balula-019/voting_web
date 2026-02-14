package com.yudhassif.election.entity;


import com.yudhassif.election.candidates.Position;
import jakarta.persistence.*;
import lombok.*;

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

    private LocalDateTime votingStartTime;

    private LocalDateTime votingEndTime;

    @OneToMany(mappedBy = "election",fetch = FetchType.LAZY)
    private List<Position> positions;
}
// vote is the main action so so we just ignored the relationship with vote because vote will be calculated in the database
