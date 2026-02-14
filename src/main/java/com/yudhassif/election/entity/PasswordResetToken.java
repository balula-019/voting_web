package com.yudhassif.election.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
    @ManyToOne(optional = false)
    private User user;
    @Column(nullable = false)
    private boolean used = false;
//    @Column(nullable = false)
    private String resetToken;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false,name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

