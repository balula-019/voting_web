package com.yudhassif.election.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class ApplyRequest {
//    private Long electionId;
//    private Long positionId;
//    private String manifesto;
//}
@Data
public class CandidateApplyRequest {

    @NotNull(message = "Position id is required")
    private Long positionId;
    @NotNull(message = "Election id is required")
    private Long electionId;


    @NotNull
    @DecimalMin(value = "3.5", message = "Minimum GPA is 3.5")
    private Double gpa;


    private String manifesto;

    // uploaded file handled separately
}

// when student apply admin look registration number of that student and look his/her gpa if gpa > 3.5 should allow that student

