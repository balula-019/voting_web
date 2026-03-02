package com.yudhassif.election.request;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(

        @NotBlank(message = "Rejection reason is required")
        String reason

) {}

