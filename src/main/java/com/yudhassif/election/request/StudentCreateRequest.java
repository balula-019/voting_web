package com.yudhassif.election.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder
@Getter
@Setter
public class StudentCreateRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "lastname is required")
    private String lastName;

    @NotBlank(message = "Registration number is required")
    private String regNumber;

    // Department code: CSE, ETE
    @NotBlank(message = "Department code is required")
    private String departmentCode;

    private int studyYear;

    // Course code: CS, EE, BIT, TE
    @NotBlank(message = "Course code is required")
    private String courseCode;
    @NotBlank(message = "Student email is required")
    private String mail;

}




// email, firstname, regNumber,departmentCode(ETE,CSE), COURSE(TE,EE,BIT,CS,CE,ES)