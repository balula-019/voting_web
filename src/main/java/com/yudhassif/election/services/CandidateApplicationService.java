package com.yudhassif.election.services;

import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.candidates.Leader;
import com.yudhassif.election.candidates.Position;
import com.yudhassif.election.entity.*;
import com.yudhassif.election.exception.BusinessException;
import com.yudhassif.election.exception.StudentNotFoundException;
import com.yudhassif.election.repository.CandidateApplicationRepository;
import com.yudhassif.election.repository.ElectionRepository;
//import com.yudhassif.election.repository.LeaderRepository;
import com.yudhassif.election.repository.PositionRepository;
import com.yudhassif.election.response.CandidateApplicationResponse;
import com.yudhassif.election.response.LeadersResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateApplicationService {

    private final CandidateApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final ElectionRepository electionRepository;
    private final PositionRepository positionRepository;
    private final FileStorageService fileStorageService;
    private final CandidateApplicationRepository repository;
//    private final LeaderRepository leaderRepository;
//    public void apply(Long studentId, CandidateApplyRequest request, MultipartFile photo) {
//
//        // 1️⃣ Load student
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        // 2️⃣ Load election
//        Election election = electionRepository.findById(request.getElectionId())
//                .orElseThrow(() -> new RuntimeException("Election not found"));
//
//        // 3️⃣ Check application window
//        if (!election.isApplicationOpen()) {
//            throw new RuntimeException("Application period closed");
//        }
//
//        // 4️⃣ Load position
//        Position position = positionRepository.findById(request.getPositionId())
//                .orElseThrow(() -> new RuntimeException("Position not found"));
//
//        // 5️⃣ Validate position belongs to election
//        if (!position.getElection().getId().equals(election.getId())) {
//            throw new RuntimeException("Position does not belong to this election");
//        }
//
//        // 6️⃣ Prevent duplicate application
//        if (applicationRepository.existsByStudentIdAndElectionId(studentId, election.getId())) {
//            throw new RuntimeException("You already applied in this election");
//        }
//
//        // 7️⃣ GPA validation
//        if (request.getGpa() < 3.5) {
//            throw new RuntimeException("GPA below required threshold");
//        }
//
//        // 8️⃣ Upload campaign photo
//        String imageUrl = fileStorageService.save(photo);
//
//        // 9️⃣ Save application as PENDING
//        CandidateApplication application = CandidateApplication.builder()
//                .student(student)
//                .election(election)
//                .position(position)
//                .gpa(request.getGpa())
//                .manifesto(request.getManifesto())
//                .campaignImageUrl(imageUrl)
//                .status(ApplicationStatus.PENDING)
//                .appliedAt(LocalDateTime.now())
//                .build();
//
//        applicationRepository.save(application);
//    }



    @Transactional
    public CandidateApplicationResponse apply(
            Long electionId,
            Long positionId,
            String manifesto,
            Double gpa,
            MultipartFile photo,

            Authentication authentication
    ) {

        // 1️⃣ Get logged-in user
        User user = (User) authentication.getPrincipal();

        // 2️⃣ Map user → student
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Student not found"));

        // 3️⃣ Validate GPA
        if (gpa < 3.5) {
            throw new BusinessException("Minimum GPA required is 3.5");
        }
        if (gpa > 5) {
            throw new BusinessException("Invalid GPA");
        }

        // 4️⃣ Get election
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new BusinessException("Election not found"));

//        if (!election.isApplicationOpen()) {
//            throw new BusinessException("Application window closed");
//        }

        // 5️⃣ Get position
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException("Position not found"));

        // 6️⃣ Prevent duplicate application
//        if (repository.existsByStudentIdAndPositionId(student.getId(), positionId)) {
//            throw new BusinessException("Already applied for this position");
//        }
        // student can apply only one position per election
        if (repository.existsByStudentIdAndElectionId(student.getId(), electionId)) {
            throw new BusinessException("You already applied in this election");
        }

        //todo the student should apply one position per election

        // 7️⃣ Store photo locally
        String photoUrl = fileStorageService.save(photo);

        // 8️⃣ Create application
        CandidateApplication app = CandidateApplication.builder()
                .student(student)
                .election(election)
                .position(position)
                .gpa(gpa)
                .manifesto(manifesto)
                .campaignImageUrl(photoUrl)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
//                .manifesto(manifesto)  // todo include manifesto
                .build();

        CandidateApplication saved = repository.save(app);

        // 9️⃣ Return response
        return CandidateApplicationResponse.builder()
                .id(saved.getId())
                .studentId(student.getId())
                .electionId(electionId)
                .positionId(positionId)
                .gpa(gpa)
                .photoUrl(photoUrl)
                .manifesto(manifesto)
                .status(saved.getStatus().name())
                .build();
    }
//    @org.springframework.transaction.annotation.Transactional(readOnly = true)
//    public List<LeadersResponse> getApprovedLeaders(Long electionId) {
//
//        List<CandidateApplication> approvedApplications =
//                applicationRepository.findByElectionIdAndStatus(
//                        electionId,
//                        ApplicationStatus.APPROVED
//                );
//
//        return approvedApplications.stream()
//                .map(application -> {
//
//                    Student student = application.getStudent();
//                    Position position = application.getPosition();
//
//                    return new LeadersResponse(
//                            student.getFirstName() + " " + student.getLastName(),
//                            position.getName(),
//                            electionId,
//                            student.getPhotoUrl()
//                    );
//                })
//                .toList();
//    },
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<LeadersResponse> getApprovedLeaders(Long electionId) {

        List<CandidateApplication> approvedApplications =
                applicationRepository.findByElectionIdAndStatus(
                        electionId,
                        ApplicationStatus.APPROVED
                );

        return approvedApplications.stream()
                .map(application -> {

                    Student student = application.getStudent();
                    Position position = application.getPosition();

                    return new LeadersResponse(
                            student.getFirstName() + " " + student.getLastName(),
                            position.getName(),
                            electionId,
                            application.getCampaignImageUrl()  // ✅ FIXED
                    );
                })
                .toList();
    }

}




    //end




    // =============================
    // STUDENT APPLY
    // =============================
//    public void apply(Long studentId, ApplyRequest request) {
//
//        Election election = electionRepository.findById(request.getElectionId())
//                .orElseThrow(() -> new BusinessException("Election not found"));
//
//        if (!election.isApplicationOpen()) {
//            throw new BusinessException("Application window closed");
//        }
//
//        if (repository.existsByStudentIdAndElectionId(studentId, election.getId())) {
//            throw new BusinessException("Already applied in this election");
//        }
//
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new BusinessException("Student not found"));
//
//        Position position = positionRepository.findById(request.getPositionId())
//                .orElseThrow(() -> new BusinessException("Position not found"));
//
//        CandidateApplication app = CandidateApplication.builder()
//                .student(student)
//                .election(election)
//                .position(position)
//                .manifesto(request.getManifesto())
//                .status(ApplicationStatus.PENDING)
//                .appliedAt(LocalDateTime.now())
//                .build();
//
//        repository.save(app);
//    }

//
//    // =============================
//    // ADMIN APPROVE
//    // =============================
//    public void approve(Long applicationId,
//                        Double gpa,
//                        MultipartFile campaignPhoto,
//                        Long adminId) {
//
//        CandidateApplication app = repository.findById(applicationId)
//                .orElseThrow(() -> new BusinessException("Application not found"));
//
//        if (!app.getElection().isApplicationClosed()) {
//            throw new BusinessException("Approval allowed only after application closes");
//        }
//
//        if (gpa < 3.5) {
//            throw new BusinessException("GPA must be >= 3.5");
//        }
//
//        String photoUrl = fileStorageService.upload(campaignPhoto);
//
//        app.setGpa(gpa);
//        app.setCampaignPhotoUrl(photoUrl);
//        app.setStatus(ApplicationStatus.APPROVED);
//        app.setApprovedByAdminId(adminId);
//        app.setApprovedAt(LocalDateTime.now());
//    }
//
//    // =============================
//    // ADMIN REJECT
//    // =============================
//    public void reject(Long id) {
//        CandidateApplication app = repository.findById(id)
//                .orElseThrow(() -> new BusinessException("Application not found"));
//
//        app.setStatus(ApplicationStatus.REJECTED);
//    }
//
//    // =============================
//    // STUDENT VIEW BALLOT
//    // =============================
//    public List<PositionCandidatesResponse> getApprovedCandidates(Long electionId) {
//
//        List<Position> positions = positionRepository.findByElectionId(electionId);
//
//        return positions.stream().map(position -> {
//
//            List<CandidateViewResponse> candidates =
//                    repository.findByElectionIdAndStatus(electionId, ApplicationStatus.APPROVED)
//                            .stream()
//                            .filter(a -> a.getPosition().getId().equals(position.getId()))
//                            .map(a -> CandidateViewResponse.builder()
//                                    .studentId(a.getStudent().getId())
//                                    .studentName(a.getStudent().getFullName())
//                                    .course(a.getStudent().getCourse())
//                                    .campaignPhoto(a.getCampaignPhotoUrl())
//                                    .manifesto(a.getManifesto())
//                                    .build())
//                            .toList();
//
//            return PositionCandidatesResponse.builder()
//                    .positionId(position.getId())
//                    .positionName(position.getName())
//                    .candidates(candidates)
//                    .build();
//
//        }).toList();
//    }


