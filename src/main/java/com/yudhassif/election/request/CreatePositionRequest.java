package com.yudhassif.election.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePositionRequest {
    @NotNull(message = "Position name is required") // like chairperson, mbunge wa mwaka mwaka wa 1,2,3,4
    private String name;

    @NotNull(message = "Election ID is required")
    private Long electionId;
}
// todo to test for approve admin