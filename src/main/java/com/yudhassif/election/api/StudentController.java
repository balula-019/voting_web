package com.yudhassif.election.api;

import com.yudhassif.election.profile.response.StudentDashboardResponse;
import com.yudhassif.election.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@RequestMapping("/api/v1/student")
public class StudentController {
    private final StudentService studentService;
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")

    @GetMapping("/me")
    public ResponseEntity<StudentDashboardResponse> studentDashboard(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(studentService.buildStudentDashboard(user));
    }
}
