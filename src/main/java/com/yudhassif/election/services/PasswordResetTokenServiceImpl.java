package com.yudhassif.election.services;

import com.yudhassif.election.entity.PasswordResetToken;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.repository.PasswordResetTokenRepository;
import com.yudhassif.election.repository.PasswordResetTokenService;
import com.yudhassif.election.repository.UserRepository;
import com.yudhassif.election.token.TokenHasher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl
        implements PasswordResetTokenService {

    private static final int TOKEN_TTL_MINUTES = 10;

    private final PasswordResetTokenRepository repository;
    private final UserRepository userRepository;
    private final TokenHasher tokenHasher;

    @Override
    @Transactional
    public void create(String rawToken, long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        String hashedToken = tokenHasher.hash(rawToken);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(hashedToken);
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES)
        );

        repository.save(resetToken);
    }

    @Override
    public Optional<PasswordResetToken> validate(String rawToken) {

        Optional<PasswordResetToken> tokenOpt =
                repository.findByResetTokenAndUsedFalse(rawToken);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            repository.save(resetToken);
            return Optional.empty();
        }

        return Optional.of(resetToken);
    }
}


