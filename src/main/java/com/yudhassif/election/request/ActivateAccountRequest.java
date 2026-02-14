package com.yudhassif.election.request;

import jakarta.validation.constraints.NotNull;

public record ActivateAccountRequest(
        @NotNull(message = "Token is required")
        String token,
        @NotNull(message = "Insert password")
        String newPassword,
        @NotNull(message = "Repeat your password")
        String confirmPassword
) {}
