package com.yudhassif.election.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Component
public class HmacOtpHasher implements OtpHasher {

    @Value("${app.security.otp.secret}")
    private String secret;

    @Override
    public String hash(String rawOtp) {
        return hmacSha256(rawOtp);
    }

    @Override
    public boolean matches(String rawOtp, String hashedOtp) {
        return hmacSha256(rawOtp).equals(hashedOtp);
    }

    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key =
                    new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("OTP hashing failed", e);
        }
    }
}

