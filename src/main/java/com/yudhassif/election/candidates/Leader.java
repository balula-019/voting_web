package com.yudhassif.election.candidates;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leaders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
}
