package com.yudhassif.election.request;
import com.yudhassif.election.entity.Semester;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class StudentCreateElectionRequest {
    @NotNull(message = "election name is required")
    private String electionName;
    @NotNull(message = "Description for the election name is required")
    private  String description;
    @NotNull(message = "Academic year is required")
    private String academicYear; // 2024/2025
    @Enumerated(EnumType.STRING)
    private Semester semester;
    @Column(nullable = false)
    private Instant startTime;
    @Column(nullable = false)
    private Instant endTime;

}
// election_name, Description, Academic Year, Semester(firstSemester,secondsemester), time of voting start, time of voting end