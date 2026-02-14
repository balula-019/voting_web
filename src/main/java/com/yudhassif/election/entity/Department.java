package com.yudhassif.election.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private String departmentCode;   // ETE, CSE

    @Column(nullable = false)
    private String name;   // Electronics & Telecommunication Engineering

    private boolean active = true;

    public Department(Long id, String name, String departmentCode) {
        this.id = id;
        this.name = name;
        this.departmentCode = departmentCode;
    }
}
// Department cant change so no any relation concerning on department I will keep on other entity and not department because student can finish university hence can change ,also can retire the course
