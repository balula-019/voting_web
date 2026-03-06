package com.yudhassif.election.api;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.profile.response.AdminDashboardResponse;
import com.yudhassif.election.request.CreatePositionRequest;
import com.yudhassif.election.request.RejectRequest;
import com.yudhassif.election.request.StudentCreateElectionRequest;
import com.yudhassif.election.request.StudentCreateRequest;
import com.yudhassif.election.response.ElectionResponse;
import com.yudhassif.election.response.ImportResultResponse;
import com.yudhassif.election.response.PositionResponse;
import com.yudhassif.election.response.StudentResponse;
import com.yudhassif.election.services.ElectionService;
import com.yudhassif.election.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.relation.RoleNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final StudentService studentService;
    private final ElectionService service;

//
//    @PostMapping("/import-student")
//    @PreAuthorize("hasAuthority('STUDENT_IMPORT')")   // done
//    public ResponseEntity<ImportResultResponse> importStudents(
//            @RequestParam("file") MultipartFile file) throws IOException {
//
//        return ResponseEntity.ok(studentService.importStudentsFromExcel(file));
//
//    }
@PostMapping(
        value = "/import-student",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
@PreAuthorize("hasAuthority('STUDENT_IMPORT')")
public ResponseEntity<ImportResultResponse> importStudents(
        @RequestParam("file") MultipartFile file
) throws IOException {

    return ResponseEntity.ok(
            studentService.importStudentsFromExcel(file)
    );
}


    @PostMapping("/create-student")// done
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public ResponseEntity<String> createStudent(@RequestBody @Valid StudentCreateRequest request) throws RoleNotFoundException {
        studentService.createStudent(request);

        return ResponseEntity.ok("Student Created Successfully");
    }

    @PostMapping("/create-election")     // done
    @PreAuthorize("hasAuthority('ELECTION_CREATE')")
    public ResponseEntity<String> createStudentElection(@RequestBody @Valid StudentCreateElectionRequest request) {
        service.createStudentElection(request);

        return ResponseEntity.accepted().build();
    }

    // Manually close election
    @PutMapping("/{id}/close")   //done
    @PreAuthorize("hasAuthority('CLOSE_ELECTION')")
    public ResponseEntity<Void> closeElection(@PathVariable Long id) {
        service.closeElection(id);
        return ResponseEntity.ok().build();
    }
    // suspend the election - to stop the election when its active
    @PutMapping("/{id}/suspend") // done
    @PreAuthorize("hasAuthority('SUSPEND_ELECTION')")
    public ResponseEntity<Void> suspendElection(@PathVariable Long id) {
        service.suspendElection(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/resume") //done
    @PreAuthorize("hasAuthority('RESUME_ELECTION')")
    public ResponseEntity<Void> resumeElection(@PathVariable Long id) {
        service.resumeElection(id);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/update-student")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')") // done
    public ResponseEntity<String> updateStudent(@RequestBody @Valid StudentCreateRequest request) {
        studentService.updateStudent(request);

        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/delete-student")  //done
    @PreAuthorize("hasAuthority('STUDENT_DELETE')")
    public ResponseEntity<Void> deleteStudent(@RequestBody @Valid StudentCreateRequest request) {
        studentService.deleteStudent(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/all")   //done
    @Transactional(readOnly = true)
    public ResponseEntity<List<ElectionResponse>> findAllElections(){
        return ResponseEntity.ok(service.findAllElections());
    }

    @PutMapping("/{id}/open")    //done
    @PreAuthorize("hasAuthority('ELECTION_OPEN')")
    public ResponseEntity<Void> openElection(@RequestBody @PathVariable Long id) {
        service.openElection(id);
        return ResponseEntity.accepted().build();
    }
    @PutMapping("/{id}/update-election")   //done
    @PreAuthorize("hasAuthority('ELECTION_UPDATE')")
    public ResponseEntity<Optional<Election>> updateElection(@RequestBody @Valid StudentCreateElectionRequest request, @PathVariable Long id) {
        service.updateElection(id, request);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/delete-election")   //done
    @PreAuthorize("hasAuthority('ELECTION_DELETE')")
    public ResponseEntity<Optional<Election>> updateElection( @PathVariable Long id) {
        service.deleteElection(id);
        return ResponseEntity.ok().build();
    }
    // endpoint to find all student
    @GetMapping("/students/{id}") //done
    @Transactional(readOnly = true)
    public ResponseEntity<List<StudentResponse>> findAllStudentById(@PathVariable Long id){
        return ResponseEntity.ok(service.findAllStudentById(id));
    }

    @GetMapping("/students/reg-number/{regNumber}") //done
    @Transactional(readOnly = true)
    public ResponseEntity<StudentResponse> findByRegNo(@PathVariable("regNumber") String regNumber){
        return ResponseEntity.ok(service.findStudentByRegistrationNumber(regNumber));
    }

    @GetMapping("/students/student-name/{first-name}/{last-name}") //done
    @Transactional(readOnly = true)
    public ResponseEntity<StudentResponse> findByFirstName(@PathVariable("first-name") String firstName, @PathVariable("last-name") String lastName){
        return ResponseEntity.ok(service.findStudentByFirstNameAndLastName(firstName,lastName));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(studentService.buildAdminDashboard(user));
    }
    @PostMapping("/create-position")
    public ResponseEntity<PositionResponse> createPosition(@Valid @RequestBody CreatePositionRequest request) {
        PositionResponse position = studentService.createPosition(request);
        return ResponseEntity.ok(position);
    }
    @PreAuthorize("hasAuthority('CANDIDATE_ASSIGN')")
    @PutMapping("/applications/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id,
                                        @AuthenticationPrincipal User admin) {
        service.approveApplication(id, admin.getId());
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAuthority('CANDIDATE_ASSIGN')")
    @PutMapping("/applications/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id,
                                       @RequestBody RejectRequest request,
                                       @AuthenticationPrincipal User admin) {
        service.rejectApplication(id, admin.getId(), request.reason());
        return ResponseEntity.ok().build();
    }




// todo resend activation token for each user if its expired
    // todo to test how student elect their leader
    // todo to logOut for admin and student
    // todo to to implement the permission for student  to view live results
    // todo to Announce  winner after 20 minute after election
}



































//@GetMapping("/students/{id}")
//public ResponseEntity<?> findStudentById(@PathVariable Long id)
//
//@GetMapping("/students/reg-number/{regNumber}")
//public ResponseEntity<?> findByRegNo(@PathVariable String regNumber)




// todo to add student leader in the database their profile and permission to edit
//todo to return in the admin dashboard - total student,total vote cast,failed vote, active elections, total candidates who want to vote
    // todo to deals with chart - like histogram, circle found in admin dashboard.

// todo flow - Admin imports students → student activated through email,regNo and password, but id inactive for the first time  Students get voterId → Election opens(after created)-done → Students vote → Results are calculated → Charts display results


// STUDENT_DELETE,CLOSE_ELECTION,ELECTION_OPEN,STUDENT_DELETE,STUDENT_UPDATE,RESUME_ELECTION,CLOSE_ELECTION,SUSPEND_ELECTION,ELECTION_CREATE

// todo tomorrow to check and ensure the election follow the exact time and @Schedule work as expected and to resolve the case of status to be active and when frontend press the button of open election
// todo to activate student
