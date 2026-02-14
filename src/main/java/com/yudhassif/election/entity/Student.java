package com.yudhassif.election.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student")
@Builder
@Getter
@Setter
@NoArgsConstructor  // Default constructor
@AllArgsConstructor
public class Student {      // so the professional implementation should be just only entity User will implement userDetails and hence we take as security principal but other like teacher,student can be just domain so should implement userDEtails even if their login credential is differnt may be student login using student email while admin login usind normal email

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // link to login principal
    @Column(nullable = false)// should contain only later
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(unique = true,nullable = true)
    private String regNumber;
    @Column(unique = true,nullable = false)
    private String email;
    @Column(unique = true,nullable = false)
    private String mail;
    @Column(nullable = true)
    private String voterId;
    @Column(nullable = false)
    private int studyYear;
    private boolean activated;
    private boolean voted;
//    private String password;  // should stay in where we implement userDetails

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}











































//package com.yudhassif.election.entity;
//
//import com.yudhassif.election.role.Role;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Min;
//import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Entity
//@Builder
//@Getter
//@Setter
//@NoArgsConstructor  // Default constructor
//@AllArgsConstructor
//@Table(name = "student")
//
//public class Student implements UserDetails {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Auto-generated voter ID (VTR20250001)
//    @Column(unique = true, nullable = true)
//    private String voterId;
//    @Column(name = "first_name", nullable = false)
//    private String firstName;
//    @Column(name = "last_name", nullable = false)
//    private String lastName;
//    @Column(unique = true,nullable = false)
//    private String regNumber;
//
//    @Column(unique = true,nullable = false)
//    private String email;
//
//
//    @Column(unique = true,nullable = false)
//    private String mail;
//
//
//
//
//
//
//    private boolean enabled = false;
//
//    private boolean activated; // email activated?
//
//    private boolean voted = false; // already voted?
//
//    @Min(value = 1, message = "Year of study must be at least 1")
//    private int studyYear;
//
//
//    @Column(nullable = true)
//    private String password; // encrypted
//
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "role_id",nullable = false)
//    private Role role; // STUDENT
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "department_id")
//    private Department department; // like ete, cse
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "course_id")
//    private Course course;   // like Bsc In Telecommunication engineering
//
//   @Override
//   public Collection<? extends GrantedAuthority> getAuthorities() {
//       List<GrantedAuthority> authorities = new ArrayList<>();
//
//// 1️⃣ Add role
//       authorities.add(
//               new SimpleGrantedAuthority("ROLE_" + role.getName())
//       );
//
//// 2️⃣ Add permissions
//       role.getPermissions().forEach(permission ->
//               authorities.add(
//                       new SimpleGrantedAuthority(permission.getName())
//               )
//       );
//       return authorities;
//   }
//
////
////        Set<GrantedAuthority> authorities = new HashSet<>();
////
////        // 1️⃣ Add ROLE
////        authorities.add(
////                new SimpleGrantedAuthority("ROLE_" + role.getName())
////        );
////
////        // 2️⃣ Add PERMISSIONS
////        role.getPermissions()
////                .forEach(permission ->
////                        authorities.add(
////                                new SimpleGrantedAuthority(permission.getName())
////                        )
////                );
////
//
//
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {  // hence student login using student email
//        return mail;
//    }
//}
//
////to enable relation between student and department and course
