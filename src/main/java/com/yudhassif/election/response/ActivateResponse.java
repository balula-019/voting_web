package com.yudhassif.election.response;

import java.time.Instant;

public record ActivateResponse(
        String token,
        Instant expiresAt
) {}