package com.yudhassif.election.otp;

public interface OtpHasher {

    String hash(String rawOtp);

    boolean matches(String rawOtp, String hashedOtp);
}

