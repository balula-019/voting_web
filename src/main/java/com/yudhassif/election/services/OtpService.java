package com.yudhassif.election.services;


import com.yudhassif.election.entity.PasswordOtp;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.otp.OtpHasher;
import com.yudhassif.election.repository.PasswordOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final PasswordOtpRepository otpRepository;
    private final OtpHasher otpHasher;
    @Value("${app.otp.max-attempts}")
    private  int MAX_ATTEMPTS;
    @Value("${app.otp.ttl-seconds}")
    private  long OTP_TTL_SECONDS;
    private final SecureRandom random = new SecureRandom();
    //method to generate six digit otp
    public String generateSixDigitOtpRaw(){
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    // method to create and save otp
    public String createAndSaveOtp(User user) {

        // 1️⃣ Generate raw OTP
        String otpRaw = generateSixDigitOtpRaw();

        // 2️⃣ Hash OTP (CRITICAL)
        String otpHash = otpHasher.hash(otpRaw);

        PasswordOtp passwordOtp = new PasswordOtp();
        passwordOtp.setUser(user);
        passwordOtp.setOtpHash(otpHash); // HASHED
        passwordOtp.setUsed(false);
        passwordOtp.setAttempts(0);
        passwordOtp.setMaxAttempts(MAX_ATTEMPTS);
        passwordOtp.setCreatedAt(LocalDateTime.now());
        passwordOtp.setExpiresAt(LocalDateTime.now().plusSeconds(OTP_TTL_SECONDS));

        otpRepository.save(passwordOtp);

        // 3️⃣ Return RAW OTP (for email only)
        return otpRaw;
    }

    // once you create and is in the database now we validate so we first fetch
    public Boolean validateOtp(User user, String otpRaw) {
        List<PasswordOtp> list = otpRepository.findByUserIdOrderByExpiresAtDesc(user.getId());
        Optional<PasswordOtp> match = list.stream()
                .filter(p-> !p.isUsed() && p.getOtpHash().equals(otpRaw) && p.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst();
        // the otp should not be used, otp given to user should be equal to that in the database and should expire within current time

        if (match.isPresent()){
            PasswordOtp p = match.get(); // return to user
            p.setUsed(true);
            otpRepository.save(p);
            return true;

        }
        if (!list.isEmpty()){
            PasswordOtp latest = list.get(0); // return the latest otp
            if (!latest.isUsed() && latest.getExpiresAt().isAfter(LocalDateTime.now())){
                latest.setAttempts(latest.getAttempts() + 1); // this take the current value but start from where we declare which is 0
                latest.setUsed(true);
                otpRepository.save(latest);
            }
            if (latest.getAttempts() >= MAX_ATTEMPTS){
                latest.setUsed(true);  // lock this otp
            }
        }

        return false; // if the validation failed that's why we return false
    }
    // method to delete expired otp
    public void deleteExpiredOtp(){
        otpRepository.deleteByExpiresAtBefore(Instant.now());
    }



}
// todo later rate limiting of otp and delete expired otp in order to reduce the database queries