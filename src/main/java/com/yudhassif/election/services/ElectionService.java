package com.yudhassif.election.services;
import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.candidates.Leader;
import com.yudhassif.election.candidates.Position;
import com.yudhassif.election.entity.*;
import com.yudhassif.election.exception.BusinessException;
import com.yudhassif.election.exception.ElectionNotFoundException;
import com.yudhassif.election.mapper.ElectionMapper;
import com.yudhassif.election.mapper.StudentMapper;
import com.yudhassif.election.repository.*;
import com.yudhassif.election.request.StudentCreateElectionRequest;
import com.yudhassif.election.response.ElectionResponse;
import com.yudhassif.election.response.StudentResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.StringUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class ElectionService {
    private final ElectionMapper mapper;
    private final StudentMapper studentMapper;
    private final StudentRepository studentRepository;
    private final ElectionRepository electionRepository;
    private final CandidateApplicationRepository candidateApplicationRepository;
    private final PositionRepository positionRepository;
    private final LeaderRepository leaderRepository;
    private final Clock clock;
    private final VotingCredentialRepository votingCredentialRepository;

    public void createStudentElection(StudentCreateElectionRequest request) {
// optional
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException(
                    "Voting start time must be before end time"
            );

        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and End time must not be null");
        }


        // Optional rule: only one ACTIVE or PENDING election
//        if (electionRepository.existsByStatus(ElectionStatus.ACTIVE)) {
//            throw new IllegalStateException(
//                    "An active election already exists"
//            );
//        }


        Election election = Election.builder()
                .electionName(request.getElectionName())
                .description(request.getDescription())
                .academicYear(request.getAcademicYear())
                .semester(request.getSemester())
                .votingStartTime(request.getStartTime())
                .votingEndTime(request.getEndTime())
                .status(ElectionStatus.PENDING)
                .build();

        electionRepository.save(election);
    }
//    public void createStudentElection(StudentCreateElectionRequest request) {
//
//        if (request.getStartTime() == null || request.getEndTime() == null) {
//            throw new IllegalArgumentException("Start time and End time must not be null");
//        }
//
//        if (request.getStartTime().isAfter(request.getEndTime())) {
//            throw new IllegalArgumentException("Voting start time must be before end time");
//        }
//
//        ElectionStatus status =
//                determineStatus(request.getStartTime(), request.getEndTime());
//
//        Election election = Election.builder()
//                .electionName(request.getElectionName())
//                .description(request.getDescription())
//                .academicYear(request.getAcademicYear())
//                .semester(request.getSemester())
//                .votingStartTime(request.getStartTime())
//                .votingEndTime(request.getEndTime())
//                .status(status) // ⭐ dynamically determined
//                .build();
//
//        electionRepository.save(election);
//    }

//    private ElectionStatus determineStatus(Instant start, Instant end) {
//
//        Instant now = Instant.now(clock);
//
//        if (now.isBefore(start)) {
//            return ElectionStatus.PENDING;
//        }
//
//        if (now.isAfter(end)) {
//            return ElectionStatus.CLOSED;
//        }
//
//        return ElectionStatus.ACTIVE;
//    }
    // Prevents voting when status is not active
    public void vote(Long electionId) {

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        if (election.getStatus() != ElectionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Voting is not allowed for this election"
            );
        }

        // proceed with voting
    }
    // This make the election to automatically start at the time and closed at the time
//



    //todo for now election will be manually until i resolve the issue of scheduling in time
//    @Scheduled(fixedDelay = 10000)// touches only active and pending other authorities like resume and suspending is for admin
//    @org.springframework.transaction.annotation.Transactional(readOnly = true)
//    public void updateElectionStatus() {
//        Instant now = Instant.now(clock);
//
//        // 1️⃣ START elections automatically
//        electionRepository.findByStatus(ElectionStatus.PENDING)
//                .forEach(election -> {
//                    if (!now.isBefore(election.getVotingStartTime())) {
//                        election.setStatus(ElectionStatus.ACTIVE);
//                        electionRepository.save(election);
//                    }
//                });
//
//        // 2️⃣ END elections automatically
//        electionRepository.findByStatus(ElectionStatus.ACTIVE)
//                .forEach(election -> {
//                    if (now.isAfter(election.getVotingEndTime())) {
//                        election.setStatus(ElectionStatus.CLOSED);
//                        electionRepository.save(election);
//                    }
//                });
//    }
    @Scheduled(fixedDelay = 10000) // Runs every 10 seconds
    @Transactional
    public void updateElectionStatuses() {
        Instant now = Instant.now(clock);

        System.out.println("------------: " + now);

        int activated = electionRepository.activatePendingElections(now);
        int closed = electionRepository.closeActiveElections(now);

        if (activated > 0 || closed > 0) {
            System.out.println("Election Update: Activated " + activated + ", Closed " + closed);
        }
    }
// logic for admin to close the election

    public void closeElection(Long electionId) {

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        if (election.getStatus() == ElectionStatus.CLOSED) {
            throw new IllegalStateException("Election already closed");
        }

        election.setStatus(ElectionStatus.CLOSED);
        electionRepository.save(election);
    }
    // Suspend the election
    public void suspendElection(Long electionId) {   //done

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        if (election.getStatus() == ElectionStatus.ACTIVE) {
            election.setStatus(ElectionStatus.SUSPENDED);
        }
        else{
            throw new IllegalStateException("Only active elections can be suspended");

        }
        electionRepository.save(election);

    }
    // resume election
    public void resumeElection(Long electionId) { // done

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        if (election.getStatus() == ElectionStatus.SUSPENDED) {
            election.setStatus(ElectionStatus.ACTIVE);
        }
        else {
            throw new IllegalStateException("Only suspended elections can be resumed");
        }
        electionRepository.save(election);

    }
// method to open election
    public void openElection(Long electionId) {

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() ->
                        new RuntimeException("Election not found"));

        if (election.getStatus() == ElectionStatus.ACTIVE) {
            throw new RuntimeException("Election is already open");
        }

        if (election.getStatus() == ElectionStatus.CLOSED) {
            throw new RuntimeException("Closed election cannot be reopened");
        }

        election.setStatus(ElectionStatus.ACTIVE);
        electionRepository.save(election);
    }


    public List<ElectionResponse> findAllElections() {   // done
            return electionRepository.findAll()
                    .stream()
                    .map(mapper:: toElectionResponse)
                    .collect(Collectors.toList());
    }
    // method for admin to update election only when status is closed and pending
    @Transactional
    public void updateElection(long electionId, StudentCreateElectionRequest request) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(()-> new EntityNotFoundException("election not found"));
        if (election.getStatus() == ElectionStatus.CLOSED || election.getStatus() == ElectionStatus.PENDING || election.getStatus() == ElectionStatus.SUSPENDED){
            mergerElection(election, request);

        }
        else {
            throw new IllegalArgumentException("Election is in active mode ");
        }

        electionRepository.save(election);
//                .filter(election1 -> {
//                    if (election1.getStatus() == ElectionStatus.CLOSED){
//                        mergerElection(election1,request);
//                        electionRepository.save(election1);
//
//                    } else if (election1.getStatus() == ElectionStatus.PENDING) {
//                        mergerElection(election1,request);
//                        electionRepository.save(election1);
//                    } else {
//                        throw new IllegalArgumentException("Cannot Update election for such status");
//                    }
//                    return true;  // todo checked carefully this
//                });
//        return election;
    }

    private void mergerElection(Election election, StudentCreateElectionRequest request) {
        if (StringUtil.isNotBlank(request.getElectionName())) {
            election.setElectionName(request.getElectionName());
        }
        if (StringUtil.isNotBlank(request.getDescription())) {
            election.setDescription(request.getDescription());
        }
        if (StringUtil.isNotBlank(request.getAcademicYear())) {
            election.setAcademicYear(request.getAcademicYear());
        }
        if (request.getStartTime() != null) {
            election.setVotingStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            election.setVotingEndTime(request.getEndTime());
        }
        if (election.getStatus() != null) {
            election.setStatus(ElectionStatus.PENDING);
        } // todo to observe the reality of that update it will always return is that better way or there is other option

        if (request.getSemester() != null) {
            election.setSemester(request.getSemester());
        }


    }

    public void deleteElection(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(()-> new RuntimeException("Election not found"));
        if (election.getStatus() == ElectionStatus.CLOSED || election.getStatus() == ElectionStatus.PENDING){
            electionRepository.deleteById(electionId);
        }
        else {
            throw new ElectionNotFoundException("Election is in processing");
        }
    }

// find all student by id
    public List<StudentResponse> findAllStudentById(Long id) {
        return studentRepository.findById(id).stream()
                .map(studentMapper:: toStudentResponse)
                .collect(Collectors.toList());


    }
//
//    public List<StudentResponse> findAllStudentByRegistrationNumber(String regNumber) {
//
//            return studentRepository.findByRegNumber(regNumber).stream()
//                    .map(studentMapper:: toStudentResponse)
//                    .collect(Collectors.toList());
//
//
//        }
public StudentResponse findStudentByRegistrationNumber(String regNumber) {

    Student student = studentRepository.findByRegNumber(regNumber)
            .orElseThrow(() -> new RuntimeException("Student not found"));

    return studentMapper.toStudentResponse(student);
}

    public StudentResponse findStudentByFirstNameAndLastName(String firstName, String lastName) {
Student student = studentRepository.findByFirstNameAndLastName(firstName,lastName);

        return studentMapper.toStudentResponse(student);
    }

    @Transactional
    public void approveApplication(Long applicationId, Long adminId) {

        // 1️⃣ Get application
        CandidateApplication application = candidateApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));

        // 2️⃣ Ensure still pending
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("Application already processed");
        }

        // 3️⃣ Ensure election still valid
        Election election = application.getElection();

//        if (election.getEndTime().isBefore(LocalDateTime.now())) {
//            throw new BusinessException("Election already ended");
//        }
//
//        // (Optional) ensure election open
//        if (!election.isElectionOpen()) {
//            throw new BusinessException("Election not open");
//        }

        // 4️⃣ Validate position
        Position position = application.getPosition();

        // Optional: limit number of candidates per position
        int approvedCount = candidateApplicationRepository.countApprovedByPositionId(position.getId());

        if (approvedCount > 4) { // example max candidates allowed
            throw new BusinessException("Candidate limit reached for this position");
        }

        // 5️⃣ Mark application approved
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId);

        candidateApplicationRepository.save(application);

        // 6️⃣ Create leader (candidate visible to voters)
        Leader leader = Leader.builder()
                .student(application.getStudent())
                .position(position)
                .election(election)
                .gpa(application.getGpa())
                .imageUrl(application.getCampaignImageUrl())
                .manifesto(application.getManifesto())
                .approvedAt(LocalDateTime.now())
                .status(ApplicationStatus.APPROVED)
                .build();

        leaderRepository.save(leader);
    }
    @Transactional
//    public void rejectApplication(Long applicationId, Long adminId, String reason) {
//
//        CandidateApplication application = candidateApplicationRepository.findById(applicationId)
//                .orElseThrow(() -> new BusinessException("Application not found"));
//
//        if (application.getStatus() != ApplicationStatus.PENDING) {
//            throw new BusinessException("Application already processed");
//        }
//
//
//        application.setStatus(ApplicationStatus.REJECTED);
//        application.setReviewedAt(LocalDateTime.now());
//        application.setReviewedBy(adminId);
//        application.setRejectionReason(reason);
//
//        candidateApplicationRepository.save(application);
//    }
    public void rejectApplication(Long applicationId, Long adminId, String reason) {

        CandidateApplication application = candidateApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));

        // prevent rejecting again if already rejected (optional)
        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new BusinessException("Application already rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId);
        application.setRejectionReason(reason);

        candidateApplicationRepository.save(application);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Election getActiveElectionById(Long electionId) {

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        Instant now = Instant.now(clock);

        // HARD TIME VALIDATION (source of truth)
        boolean withinWindow =
                !now.isBefore(election.getVotingStartTime()) &&
                        !now.isAfter(election.getVotingEndTime());

//        if (!withinWindow) {
//            throw new IllegalStateException("Election is not in voting window");
//        }

        // STATUS VALIDATION (business lifecycle)
        if (election.getStatus() != ElectionStatus.ACTIVE) {
            throw new IllegalStateException("Election is not active");
        }

        return election;
    }
}

// we use Instant time because is used globally but in tanzania we are ahead for 3 hour UTC If the database is in AWS We can use local datetime