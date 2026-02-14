package com.yudhassif.election.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AdminLoginRequest(
        @NotNull(message = "email is required")
        String identifier,
        @NotNull(message = "email is required")
        String password) {
}
