package com.yudhassif.election.seeder;

import com.yudhassif.election.entity.Course;
import com.yudhassif.election.entity.Department;
import com.yudhassif.election.repository.CourseRepository;
import com.yudhassif.election.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Order(2)
 // run after DepartmentSeeder
public class CourseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) {
        if (courseRepository.count() > 0) return; // already seeded

        // ✅ Get or create departments

        Department cse = departmentRepository.findByDepartmentCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        new Department(null, "Computer Science and Engineering", "CSE")
                ));

        Department ete = departmentRepository.findByDepartmentCode("ETE")
                .orElseGet(() -> departmentRepository.save(
                        new Department(null, "Electronics and Telecommunication Engineering", "ETE")));

        // ✅ Seed courses safely
        if (courseRepository.findByCourseCode("CS").isEmpty()) {
            courseRepository.save(new Course(null, "CS", "Computer Science",cse));
        }
        if (courseRepository.findByCourseCode("CS").isEmpty()) {
            courseRepository.save(new Course(null, "CS", "Electronics Science", cse));
        }
        if (courseRepository.findByCourseCode("BIT").isEmpty()) {
            courseRepository.save(new Course(null, "BIT", "Business Information and Technology", cse));
        }
        if (courseRepository.findByCourseCode("EE").isEmpty()) {
            courseRepository.save(new Course(null, "EE", "Electronics Engineering", ete));
        }
        if (courseRepository.findByCourseCode("ES").isEmpty()) {
            courseRepository.save(new Course(null, "ES", "Electronics Science", ete));
        }

        if (courseRepository.findByCourseCode("TE").isEmpty()) {
            courseRepository.save(new Course(null, "TE", "Telecommunication Engineering", ete));
        }
        if (courseRepository.findByCourseCode("CEIT").isEmpty()) {
            courseRepository.save(new Course(null, "CEIT", "Computer Engineering", cse));
        }

        System.out.println("✅ Courses seeded successfully.");// in production, we remove this
    }
}





























//package com.yudhassif.election.seeder;
//
//import com.yudhassif.election.entity.Course;
//import com.yudhassif.election.entity.Department;
//import com.yudhassif.election.repository.CourseRepository;
//import com.yudhassif.election.repository.DepartmentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class CourseSeeder implements CommandLineRunner {
//
//    private final CourseRepository courseRepository;
//    private final DepartmentRepository departmentRepository;
//
//    @Override
//    public void run(String... args) {
//        if (courseRepository.count() == 0) {
//
//            Department cse = departmentRepository.findByDepartmentCode("CSE").orElseThrow();
//            Department ete = departmentRepository.findByDepartmentCode("ETE").orElseThrow();
//
//            courseRepository.save(new Course(null, "CS", "Computer Science", cse));
//            courseRepository.save(new Course(null, "BIT", "Business Information and Technology", cse));
//            courseRepository.save(new Course(null, "EE", "Electronics  Engineering", ete));
//            courseRepository.save(new Course(null, "EE", "Telecommunication Engineering", ete));
//            courseRepository.save(new Course(null, "EE", "Computer Engineering", ete));
//
//
//        }
//    }
//}
