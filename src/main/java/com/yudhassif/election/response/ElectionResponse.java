package com.yudhassif.election.response;

import java.time.LocalDateTime;

public record ElectionResponse(
        Long id,
        String electionName,
        String status,              //
        String description,
        String academicYear,        //
        LocalDateTime votingStartTime,
        String semester,
        LocalDateTime votingEndTime
)
{}
