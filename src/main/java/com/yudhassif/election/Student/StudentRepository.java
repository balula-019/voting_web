package com.yudhassif.election.Student;

import com.yudhassif.election.entity.Student;
import com.yudhassif.election.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    boolean existsByRegNumber(String regNumber);

    Optional<Student> findByRegNumber( String regNumber);

    boolean existsByVoterId(String voterId);

 Optional<Student> findByMail(String Mail);

    Student findByFirstNameAndLastName(String firstName, String lastName);


    long countByActivatedTrue();

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(long id);
}
