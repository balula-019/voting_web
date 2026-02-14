package com.yudhassif.election.services;
import com.yudhassif.election.entity.PasswordOtp;
import com.yudhassif.election.entity.PasswordResetToken;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.exception.InvalidTokenException;
import com.yudhassif.election.exception.TokenExpiredException;
import com.yudhassif.election.exception.WeakPasswordException;
import com.yudhassif.election.otp.OtpHasher;
import com.yudhassif.election.repository.PasswordOtpRepository;
import com.yudhassif.election.repository.PasswordResetTokenRepository;
import com.yudhassif.election.repository.PasswordResetTokenService;
import com.yudhassif.election.repository.UserRepository;
import com.yudhassif.election.token.TokenHasher;
import com.yudhassif.election.token.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordOtpRepository otpRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final OtpHasher otpHasher;
    private final TokenRepository tokenRepository;
    private final TokenHasher tokenHasher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Step 1: Create OTP + send email
    public String generateOtpAndSend(User user) {

        // 1️⃣ Generate + store OTP
        String otpRaw = otpService.createAndSaveOtp(user);

        // 2️⃣ Send raw OTP via email
        emailService.sendOtpEmail(user.getEmail(),
                otpRaw,
                user.getFirstName()
                ,user.getRole().getName());
        return otpRaw;
    }


    // Step 3:  reset password
    @Transactional
    public void resetPassword(String rawResetToken,
                              String newPassword,
                              String confirmPassword) {

        // 1️⃣ Validate reset token (fetch + expiry + used)
        PasswordResetToken token = validateResetToken(rawResetToken);
        User user = token.getUser();

        // 2️⃣ Confirm password match (FIRST)
        if (!newPassword.equals(confirmPassword)) {
            throw new WeakPasswordException(
                    "New password and confirm password do not match"
            );
        }

        // 3️⃣ Validate password length
        if (newPassword.length() < 8 || newPassword.length() > 64) {
            throw new WeakPasswordException(
                    "Password must be between 8 and 64 characters"
            );
        }

        // 4️⃣ Validate password complexity
        if (!newPassword.matches(".*[A-Z].*")
                || !newPassword.matches(".*[a-z].*")
                || !newPassword.matches(".*\\d.*")) {

            throw new WeakPasswordException(
                    "Password must contain uppercase, lowercase, and number"
            );
        }

        // 5️⃣ Prevent password reuse
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new WeakPasswordException(
                    "New password must be different from old password"
            );
        }

        // 6️⃣ Encode & update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 7️⃣ Invalidate ALL reset tokens for this user
        passwordResetTokenRepository.invalidateAllForUser(user.getId());
    }




    @Transactional
    public Optional<String> verifyOtpAndGenerateResetToken(String otpRaw, String email) {
        Optional<User> userOpt =userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty(); // do not leak info
        }

        User user = userOpt.get();

        Optional<PasswordOtp> otpOpt = otpRepository.findLatestOtpForUser(user.getId());
        if (otpOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordOtp storedOtp = otpOpt.get();

        if (storedOtp.isExpired() || storedOtp.isUsed()) {
            storedOtp.markUsed();
            otpRepository.save(storedOtp);
            return Optional.empty();
        }

        if (!otpHasher.matches(otpRaw, storedOtp.getOtpHash())) {
            storedOtp.incrementAttempts();
            if (storedOtp.hasExceededMaxAttempts()) {
                storedOtp.markUsed();
            }
            otpRepository.save(storedOtp);
            return Optional.empty();
        }

        // SUCCESS
        storedOtp.markUsed();
        otpRepository.save(storedOtp);

        String resetToken = UUID.randomUUID().toString();
        passwordResetTokenService.create(resetToken, user.getId());

        return Optional.of(resetToken);
    }
    private PasswordResetToken validateResetToken(String rawToken) {

        String hashed = tokenHasher.hash(rawToken);

        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(hashed)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset token expired");
        }

        return token;
    }


}


