package com.yudhassif.election.mapper;

import com.yudhassif.election.entity.Student;
import com.yudhassif.election.response.StudentResponse;
import org.springframework.stereotype.Service;

@Service


public class StudentMapper {
    public StudentResponse toStudentResponse(Student student) {

        return new StudentResponse
                (
                        student.getId(),
                        student.getEmail(),
                        student.getFirstName()
                        ,student.getLastName(),
                        student.getStudyYear(),
                        student.getRegNumber(),
                        student.getCourse().getCourseCode(),
                        student.getMail(),
                        student.getDepartment().getDepartmentCode()
                );
    }


}
