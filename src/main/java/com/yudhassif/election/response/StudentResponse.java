package com.yudhassif.election.response;


public record StudentResponse (

     Long id,
     String email,

     String firstName,
     String lastName,
     int studyYear,
     String regNumber,
     String courseCode,
     String mail,
     String departmentCode
)


{
}

