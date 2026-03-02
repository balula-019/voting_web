package com.yudhassif.election.api;

import com.yudhassif.election.profile.response.StudentDashboardResponse;
import com.yudhassif.election.request.VoteRequest;
import com.yudhassif.election.response.CandidateApplicationResponse;
import com.yudhassif.election.response.LeadersResponse;
import com.yudhassif.election.services.CandidateApplicationService;
import com.yudhassif.election.services.StudentService;
import com.yudhassif.election.services.voteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@RequestMapping("/api/v1/student")
public class StudentController {
    private final StudentService studentService;
    private final CandidateApplicationService service;
    private final voteService voteService;

    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")

    @GetMapping("/me")
    public ResponseEntity<StudentDashboardResponse> studentDashboard(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(studentService.buildStudentDashboard(user));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateApplicationResponse> apply(
            @RequestParam Long electionId,
            @RequestParam Long positionId,
            @RequestParam String manifesto,
            @RequestParam Double gpa,
            @RequestParam MultipartFile photo,
            Authentication authentication
    ) {      // todo application start time should be active if the election is active

        CandidateApplicationResponse response =
                service.apply(electionId, positionId, manifesto, gpa, photo, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/elections/{electionId}/leaders")
    public ResponseEntity<List<LeadersResponse>> getApprovedLeaders(
            @PathVariable Long electionId) {

        return ResponseEntity.ok(service.getApprovedLeaders(electionId));
    }
    @PreAuthorize("hasAuthority('VOTE_CAST')")
    @PostMapping("/{electionId}/vote")
    public ResponseEntity<String> castVote(
            @PathVariable Long electionId,
            @RequestBody VoteRequest request
    ) {

        voteService.castVote(
                request.credentialId(),
                electionId,
                request.LeaderId());

        return ResponseEntity.ok("Vote successfully cast");
    }
    // todo student to vote in election and should choose only one leader per position




}
