package com.yudhassif.election.services;
import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.ElectionStatus;
import com.yudhassif.election.entity.Student;
import com.yudhassif.election.exception.ElectionNotFoundException;
import com.yudhassif.election.exception.StudentNotFoundException;
import com.yudhassif.election.mapper.ElectionMapper;
import com.yudhassif.election.mapper.StudentMapper;
import com.yudhassif.election.repository.ElectionRepository;
import com.yudhassif.election.request.StudentCreateElectionRequest;
import com.yudhassif.election.response.ElectionResponse;
import com.yudhassif.election.response.StudentResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

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

//    private ElectionStatus determineStatus(
//            LocalDateTime start,
//            LocalDateTime end
//    ) {
//        LocalDateTime now = LocalDateTime.now();
//
//        if (now.isBefore(start)) {
//            return ElectionStatus.PENDING;
//        } else if (now.isAfter(end)) {
//            return ElectionStatus.CLOSED;
//        } else {
//            return ElectionStatus.ACTIVE;
//        }
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
//        LocalDateTime now = LocalDateTime.now(ZoneId.of("Africa/Dar_es_Salaam")); // i will remove after testing
////        Instant now = Instant.now();
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
}

// we use Instant time because is used globally but in tanzania we are ahead for 3 hour UTC If the database is in AWS We can use local datetime