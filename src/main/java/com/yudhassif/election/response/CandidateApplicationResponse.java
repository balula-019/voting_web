package com.yudhassif.election.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CandidateApplicationResponse {

    private Long id;
    private Long studentId;
    private Long electionId;
    private Long positionId;
    private Double gpa;
    private String photoUrl;
    private String status;
    private String message;
    private String manifesto;
}
