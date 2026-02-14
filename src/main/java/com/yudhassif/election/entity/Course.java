package com.yudhassif.election.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private String courseCode;   // CS, EE, BIT

    @Column(nullable = false)
    private String name; // like Computer science, TE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private boolean active = true;
    // the constructor bellow is for fixing course seeder
    public Course(Long id, String courseCode, String name, Department department) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.department = department;
    }

}
