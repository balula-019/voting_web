package com.yudhassif.election.response;

import java.time.Instant;

public record ElectionResponse(
        Long id,
        String electionName,
        String status,              //
        String description,
        String academicYear,        //
        Instant votingStartTime,
        String semester,
        Instant votingEndTime
)
{}
