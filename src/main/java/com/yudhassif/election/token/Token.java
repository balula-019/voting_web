package com.yudhassif.election.token;

import com.yudhassif.election.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Getter
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)  // This store long token safely
    private String tokenHash;
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    private boolean expired;
    private boolean used = false;
    private boolean revoked;
    // it loads the user that you want in the database
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @ToString.Exclude
    private User user;
}

