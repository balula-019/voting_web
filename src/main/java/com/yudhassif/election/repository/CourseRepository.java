package com.yudhassif.election.repository;

import com.yudhassif.election.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Course save(String courseCode);

    long count();

    Optional<Course> findByCourseCode(  String courseCode);

    Optional<Course> findFirstByDepartment_DepartmentCodeIn(List<String> departmentCodes);
}
