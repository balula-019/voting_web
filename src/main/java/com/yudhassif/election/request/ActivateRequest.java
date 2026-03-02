package com.yudhassif.election.request;

import jakarta.validation.constraints.NotBlank;

public record ActivateRequest
        (       @NotBlank(message = "Voter Id is required")
                String voterId
        ) {}
