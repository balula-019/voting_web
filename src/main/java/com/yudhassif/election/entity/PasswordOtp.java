package com.yudhassif.election.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity

@Table(name = "password_otps", indexes = {
        @Index(name = "idx_otp_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PasswordOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private LocalDateTime expiresAt; // time taken to expire
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;
    @Column(nullable = false)
    private boolean used = false; // it can not be expired but can be used
    private int attempts = 0; // Initial no any attempts
    private int maxAttempts = 5;
    private LocalDateTime createdAt;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    /* ===== State checks ===== */

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededMaxAttempts() {

        return attempts >= maxAttempts;
    }

    /* ===== State mutations (VERY IMPORTANT) ===== */

    /** Only increments attempts */
    public void incrementAttempts() {
        this.attempts++;
    }

    /** Only marks OTP as used */
    public void markUsed() {
        this.used = true;
    }
}



