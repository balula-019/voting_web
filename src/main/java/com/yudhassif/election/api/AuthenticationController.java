package com.yudhassif.election.api;
import com.yudhassif.election.profile.response.StudentDashboardResponse;
import com.yudhassif.election.request.ActivateAccountRequest;
import com.yudhassif.election.request.AdminLoginRequest;
import com.yudhassif.election.response.AuthenticationResponse;
import com.yudhassif.election.services.AuthenticationServices;
import com.yudhassif.election.services.StudentService;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationServices services;
    private final StudentService studentService;
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AdminLoginRequest request2){
        return ResponseEntity.ok(services.login(request2));
    }
    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        services.refreshToken(request, response);
    }
   @PostMapping("/activate")
//   @PreAuthorize("hasRole('STUDENT')") // is not needed
   public ResponseEntity<Void> activateStudent(@Valid @RequestBody ActivateAccountRequest request){
        services.activateStudent(request);
       return ResponseEntity.accepted().build();
   }
    @PostMapping("/resend-activation")
    public ResponseEntity<String> resendActivation(
            @RequestParam String email
    ) {
        String token = studentService.resendActivationToken(email);

        return ResponseEntity.ok("New activation token: " + token);
    }

   // the technic here is to implement interface called dashboard response then to implement as admin and student in its own
//   @PreAuthorize("hasRole('STUDENT') and hasAuthority('DASHBOARD_VIEW')")
//   @GetMapping("/profile")
//   public StudentDashboardResponse getStudentDashboard(
//           @AuthenticationPrincipal UserDetails user) {
//
//       return studentService.buildStudentDashboard(user);
//   }




}
// todo to check refresh token and insertion of token in browser to get role
// interface must declare method not field
// the concept here of having 2 implementation which inherit one interface which is dahboardResponse is called polymorphiosm in OOP