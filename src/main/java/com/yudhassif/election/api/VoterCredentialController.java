package com.yudhassif.election.api;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.request.ActivateRequest;
import com.yudhassif.election.response.ActivateResponse;
import com.yudhassif.election.response.VoterIdResponse;
import com.yudhassif.election.services.ElectionService;
import com.yudhassif.election.services.StudentService;
import com.yudhassif.election.services.VotingCredentialService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/voter-credentials")
@RequiredArgsConstructor
public class VoterCredentialController {

    private final VotingCredentialService votingCredentialService;
    private final ElectionService electionService;
    private final StudentService studentService;
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("{electionId}/generate")
    public VoterIdResponse generate(
            @PathVariable Long electionId,
            HttpServletRequest request) {

        User user = studentService.getAuthenticatedStudent();

        Election election =
                electionService.getActiveElectionById(electionId);

        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");

        String voterId = votingCredentialService.generateCredential(
                user.getStudent(),
                election,
                ip,
                device
        );

        return new VoterIdResponse(voterId);
    } // update in like in production
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{electionId}/activate")
    public ActivateResponse activate(
            @PathVariable Long electionId,
            @RequestBody ActivateRequest request
    ) {
        return votingCredentialService.activate(electionId, request.voterId());
    }
}
