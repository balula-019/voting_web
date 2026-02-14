package com.yudhassif.election.repository;


import com.yudhassif.election.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenService {

    void create(String rawToken, long userId);

    Optional<PasswordResetToken> validate(String rawToken);
}

