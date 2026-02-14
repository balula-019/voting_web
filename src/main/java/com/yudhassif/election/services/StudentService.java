package com.yudhassif.election.services;
import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.entity.*;
import com.yudhassif.election.exception.CourseNotFoundException;
import com.yudhassif.election.exception.DepartmentNotFoundException;
import com.yudhassif.election.exception.StudentNotFoundException;
import com.yudhassif.election.profile.response.AdminDashboardResponse;
import com.yudhassif.election.profile.response.StudentDashboardResponse;
import com.yudhassif.election.repository.CourseRepository;
import com.yudhassif.election.repository.DepartmentRepository;
import com.yudhassif.election.repository.ElectionRepository;
import com.yudhassif.election.repository.UserRepository;
import com.yudhassif.election.request.StudentCreateRequest;
import com.yudhassif.election.response.ImportResultResponse;
import com.yudhassif.election.role.Role;
import com.yudhassif.election.role.RoleRepository;
import com.yudhassif.election.token.ActivationToken;
import com.yudhassif.election.token.ActivationTokenRepository;
import com.yudhassif.election.token.TokenHasher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.management.relation.RoleNotFoundException;
import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ElectionRepository electionRepository;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;
    private final ActivationTokenRepository activationTokenRepository;
//    @PersistenceContext
//    private EntityManager entityManager;
    // done
//    public ImportResultResponse importStudentsFromExcel(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File is empty");
//        }
//
//        if (!file.getOriginalFilename().endsWith(".xlsx")) {
//            throw new IllegalArgumentException("Only Excel (.xlsx) files allowed");
//        }
//
//        Role studentRole = roleRepository.findByName("STUDENT")
//                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));
//
//        int total = 0;
//        int imported = 0;
//        int skipped = 0;
//
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // skip header
//                total++;
//
//                String regNumber = getStringCell(row, 0);
//                String mail = getStringCell(row, 1); //student email
//                String email = getStringCell(row, 2); // normal email
//                String studyYear = getStringCell(row, 3);
//                String firstname =  getStringCell(row, 4);
//                String lastname = getStringCell(row, 5);
//                String departmentCode = getStringCell(row, 6);
//                String courseCode = getStringCell(row, 7);
//
//                // Skip if required fields are missing
//                if (regNumber == null || email == null || departmentCode == null) {
//                    skipped++;
//                    continue;
////               throw new StudentNotFoundException("Student not found");
//                }
//
//                if ( studentRepository.existsByRegNumber(regNumber)) {
//                    skipped++;
//                    continue;
//                }
//                Course course = courseRepository.findByCourseCode(courseCode).orElseThrow(()-> new CourseNotFoundException("Course not found"));
//
//                // Fetch department safely
//                Department department = departmentRepository.findByDepartmentCode(departmentCode)
//                        .orElseThrow(()-> new DepartmentNotFoundException("Department not found"));
//                String activationToken = UUID.randomUUID().toString();
//
//                // Build and save student
//                Student student = Student.builder()
//                        .regNumber(regNumber)
//                        .email(email)
//                        .mail(mail)
//                        .studyYear(Integer.parseInt(studyYear))
//                        .firstName(firstname)
//                        .lastName(lastname)
//                        .department(department)
//                        .course(course)
//                        .voterId(null)    // generateUniqueVoterId()
//                        .enabled(false)
//                        .role(studentRole)
//                        .password(null)
//                        .build();
//                studentRepository.save(student);
//                // ✅ CREATE ACTIVATION TOKEN
//                String rawToken = UUID.randomUUID().toString();
//
//                ActivationToken token = ActivationToken.builder()
//                        .token(rawToken) // RAW for now
//                        .student(student)
//                        .expiresAt(Instant.now().plusSeconds(60 * 60 * 24))
//                        .used(false)
//                        .build();
//                activationTokenRepository.save(token);
//                imported++;
//            }
//
//            return new ImportResultResponse(total, imported, skipped);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Student import failed", e);
//        }
//    }

public ImportResultResponse importStudentsFromExcel(MultipartFile file) {

    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("File is empty");
    }

    if (!file.getOriginalFilename().endsWith(".xlsx")) {
        throw new IllegalArgumentException("Only Excel (.xlsx) files allowed");
    }

    Role studentRole = roleRepository.findByName("STUDENT")
            .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

    int total = 0;
    int imported = 0;
    int skipped = 0;

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // skip header
            total++;

            String regNumber = getStringCell(row, 0);
            String mail = getStringCell(row, 1);  // student login email
            String email = getStringCell(row, 2); // optional personal email
            String studyYear = getStringCell(row, 3);
            String firstname = getStringCell(row, 4);
            String lastname = getStringCell(row, 5);
            String departmentCode = getStringCell(row, 6);
            String courseCode = getStringCell(row, 7);

            // Skip if required fields are missing
            if (regNumber == null || mail == null || departmentCode == null) {
                skipped++;
                continue;
            }

            // Skip if student already exists
            if (studentRepository.existsByRegNumber(regNumber)) {
                skipped++;
                continue;
            }

            // Fetch course and department
            Course course = courseRepository.findByCourseCode(courseCode)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found"));
            Department department = departmentRepository.findByDepartmentCode(departmentCode)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

            // ✅ Create or fetch User for security
            User user = userRepository.findByEmail(mail)   // student is fetched by student email
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .firstName(firstname)
                                .lastName(lastname)
                                .email(mail)     // login identifier
                                .role(studentRole)
                                .enabled(false)  // activated via link
                                .password(null)  // no password yet
                                .build();
                        return userRepository.save(newUser);
                    });

            // Build and save Student profile
            Student student = Student.builder()
                    .regNumber(regNumber)
                    .email(email)   // optional personal email
                    .mail(mail)     // login email
                    .studyYear(Integer.parseInt(studyYear))
                    .firstName(firstname)
                    .lastName(lastname)
                    .department(department)
                    .course(course)
                    .voterId(null)      // generate later
                    .activated(false)   // not yet activated
                    .voted(false)
                    .user(user)         // link to User entity
                    .build();

            studentRepository.save(student);

            // ✅ Create activation token
            String rawToken = UUID.randomUUID().toString();
            ActivationToken token = ActivationToken.builder()
                    .token(rawToken)
                    .user(user)
                    .expiresAt(Instant.now().plusSeconds(60 * 60 * 24))
                    .used(false)
                    .build();
            activationTokenRepository.save(token);

            imported++;
        }

        return new ImportResultResponse(total, imported, skipped);

    } catch (Exception e) {
        throw new RuntimeException("Student import failed", e);
    }
}
    // =========================
    // ✅ HELPER METHODS
    // =========================
    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        // Handle Formulas
        if (cell.getCellType() == CellType.FORMULA) {
            // Use the cached result of the formula
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return String.valueOf((long) cell.getNumericCellValue());
                default:
                    return null;
            }
        }

        // Handle standard String cells
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }

        // Handle standard Numeric cells (like Study Year or Reg Numbers)
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }

        return null;
    }

    // 🔐 Backend-only voterId generation
    private String generateUniqueVoterId() {
        String voterId;
        do {
            voterId = "VOT-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        } while (studentRepository.existsByVoterId(voterId));
        return voterId;
    }

    private String getCell(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? null : cell.getStringCellValue().trim();
    }

//    public void createStudent(StudentCreateRequest request) throws RoleNotFoundException {
//        Role role = roleRepository.findByName("STUDENT")
//                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
//
//        Department department = departmentRepository
//                .findByDepartmentCode(request.getDepartmentCode())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid department"));
//
//        Course course = courseRepository
//                .findByCourseCode(request.getCourseCode())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid course"));
//
//        if (!course.getDepartment().getId().equals(department.getId())) {
//            throw new IllegalArgumentException("Course does not belong to department");
//        }
//
//        // ✅ Generate activation token BEFORE building student
//
//        Student student = Student.builder()
//                .email(request.getEmail())
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .regNumber(request.getRegNumber())
//                .voterId(null)
//                .department(department)
//                .course(course)
//                .role(role)
//                .mail(request.getMail()) // alternate email
//                .studyYear(request.getStudyYear())
//                .password(null)
//                .enabled(false)
//                .build();
//        // ✅ CREATE ACTIVATION TOKEN
//        String rawToken = UUID.randomUUID().toString();
//
//        ActivationToken token = ActivationToken.builder()
//                .token(rawToken) // RAW for now
//                .student(student)
//                .expiresAt(Instant.now().plusSeconds(60 * 60 * 24))
//                .used(false)
//                .build();
//      activationTokenRepository.save(token);
//        studentRepository.save(student); // save student
//        log.info("Activation token generated: {}", token); //  I should remove this in production
//
//    }
    public void createStudent(StudentCreateRequest request) throws RoleNotFoundException {
        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        Department department = departmentRepository
                .findByDepartmentCode(request.getDepartmentCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid department"));

        Course course = courseRepository
                .findByCourseCode(request.getCourseCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid course"));

        if (!course.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Course does not belong to department");
        }

        // ✅ Create or fetch User for security
        User user = userRepository.findByEmail(request.getMail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getMail())         // login identifier
                            .role(role)
                            .enabled(false)                  // activated via link
                            .password(null)                  // no password yet
                            .build();
                    return userRepository.save(newUser);  // is the field of student but should be the same field like admin
                });

        // Build and save Student profile
        Student student = Student.builder()
                .regNumber(request.getRegNumber())
                .email(request.getEmail())       // optional personal email
                .mail(request.getMail())         // login email
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(department)
                .course(course)
                .studyYear(request.getStudyYear())
                .voterId(null)                   // generate later
                .activated(false)
                .user(user)                      // link to User entity
                .build();

        studentRepository.save(student);

        // ✅ Create activation token
        String rawToken = UUID.randomUUID().toString();
        ActivationToken token = ActivationToken.builder()
                .token(rawToken)
                .user(user)                       // link token to user
                .expiresAt(Instant.now().plusSeconds(60 * 60 * 24))
                .used(false)
                .build();

        activationTokenRepository.save(token);

        log.info("Activation token generated: {}", token); // remove in production
    }




    // 🔹 Single student creation (API)


//        String activationLink = "http://localhost:8082/activate.html?token=your-test-uuid-here"
//                + student.getActivationToken();
//        emailService.sendActivationEmail(student.getEmail(), activationLink);

    @Transactional
    public void updateStudent(StudentCreateRequest request) {
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new StudentNotFoundException("Student is not found"));
        mergerStudent(student, request);//method to update Student
        studentRepository.save(student);
    }


    private void mergerStudent(Student student, StudentCreateRequest request) {
        if (StringUtil.isNotBlank(request.getRegNumber())) {
            student.setRegNumber(request.getRegNumber());
        }
        if (StringUtil.isNotBlank(request.getFirstName())) {
            student.setFirstName(request.getFirstName());
        }
        if (StringUtil.isNotBlank(request.getLastName())) {
            student.setLastName(request.getLastName());
        }
        if (StringUtil.isNotBlank(request.getEmail())) {
            student.setEmail(request.getEmail());
        }
        if (StringUtil.isNotBlank(request.getDepartmentCode())) {
            Department department = departmentRepository.findByDepartmentCode(request.getDepartmentCode())
                    .orElseThrow(() -> new DepartmentNotFoundException("Department Not Found"));
            student.setDepartment(department);
        }
        if (StringUtil.isNotBlank(request.getCourseCode())) {
            Course course = courseRepository.findByCourseCode(request.getCourseCode())
                    .orElseThrow(() -> new CourseNotFoundException("Department Not Found"));
            student.setCourse(course);
        }
        // end of update student for admin

    }


    @Transactional
    public void deleteStudent(StudentCreateRequest request) {
       Student student = studentRepository.findByEmail(request.getEmail())
               .orElseThrow(()-> new StudentNotFoundException("Student not found"));
       Student du = studentRepository.findByRegNumber(request.getRegNumber())
               .orElseThrow(()-> new StudentNotFoundException("Student not Found"));
       studentRepository.delete(student);

    }
    public StudentDashboardResponse buildStudentDashboard(UserDetails user) {

        // 🔹 Load student from DB
        Student student = studentRepository.findByMail(user.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Student not found"));

        // 🔹 Dashboard data (student-specific)
        long totalActiveElections =
                electionRepository.countByStatus(ElectionStatus.ACTIVE);

        // 🔹 Return StudentDashboardResponse
        return new StudentDashboardResponse(
                student.getFirstName(),
                student.getRegNumber(),
                student.isActivated(),
                totalActiveElections
        );
    }

//
//
//        public StudentDashboardResponse buildStudentDashboard(UserDetails user) {
//            Student student = studentRepository.findByEmail(user.getUsername())
//                    .orElseThrow(() ->
//                            new UsernameNotFoundException("Student not found"));
//
//            return new StudentDashboardResponse(
//                    student.getFirstName() + " " + student.getLastName(),
//                    student.getRegNumber()
//            );
//        }

public AdminDashboardResponse buildAdminDashboard(UserDetails user) {

    // 🔹 Load admin from DB
    User admin = userRepository.findByEmail(user.getUsername())
            .orElseThrow(() ->
                    new UsernameNotFoundException("Admin not found"));

    // 🔹 Get counts for dashboard
    long totalActivatedStudents = studentRepository.countByActivatedTrue();
    long totalStudents = studentRepository.count(); // no need to cast
    long totalElections = electionRepository.count();
    long totalActiveElections = electionRepository.countByStatus(ElectionStatus.ACTIVE);

    // 🔹 Return AdminDashboardImpl with all required data
    return new AdminDashboardResponse(
            admin.getFirstName(),
            totalActivatedStudents,
            totalStudents,
            totalElections,
            totalActiveElections
    );
}


    private boolean hasRole(UserDetails user, String role) {
            return user.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals(role));
        }
    }































































































//
//    private String getStringCell(Row row, int index) {
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        // Handle Formulas
//        if (cell.getCellType() == CellType.FORMULA) {
//            // This gets the last calculated value of the formula
//            switch (cell.getCachedFormulaResultType()) {
//                public case STRING:
//                    return cell.getStringCellValue().trim();
//                public case NUMERIC:
//                    return String.valueOf((long) cell.getNumericCellValue());
//                default:
//                    return null;
//            }
//        }
//
//        if (cell.getCellType() == CellType.STRING) {
//            return cell.getStringCellValue().trim();
//        }
//
//        if (cell.getCellType() == CellType.NUMERIC) {
//            return String.valueOf((long) cell.getNumericCellValue());
//        }
//
//        return null;
//    }

//
//    private String getStringCell(Row row, int index) {
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        if (cell.getCellType() == CellType.STRING) {
//            return cell.getStringCellValue().trim();
//        }
//
//        if (cell.getCellType() == CellType.NUMERIC) {
//            return String.valueOf((long) cell.getNumericCellValue());
//        }
//
//        return null;
//    }

//    private Integer getIntegerCell(Row row, int index) {
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        if (cell.getCellType() == CellType.NUMERIC) {
//            return (int) cell.getNumericCellValue();
//        }
//
//        if (cell.getCellType() == CellType.STRING) {
//            try {
//                return Integer.parseInt(cell.getStringCellValue().trim());
//            } catch (NumberFormatException e) {
//                return null;
//            }
//        }
//
//        return null;
//    }















































//package com.yudhassif.election.services;
//
//import com.yudhassif.election.Student.StudentRepository;
//import com.yudhassif.election.entity.Course;
//import com.yudhassif.election.entity.Department;
//import com.yudhassif.election.entity.Student;
//import com.yudhassif.election.repository.CourseRepository;
//import com.yudhassif.election.repository.DepartmentRepository;
//import com.yudhassif.election.request.StudentCreateRequest;
//import com.yudhassif.election.response.ImportResultResponse;
//import com.yudhassif.election.role.Role;
//import com.yudhassif.election.role.RoleRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class StudentService {
//
//    private final StudentRepository studentRepository;
//    private final RoleRepository roleRepository;
//    private final DepartmentRepository departmentRepository;
//    private final CourseRepository courseRepository;
//
//    public ImportResultResponse importStudentsFromExcel(MultipartFile file) {
//
//        if (!file.getOriginalFilename().endsWith(".xlsx")) {
//            throw new IllegalArgumentException("Only Excel (.xlsx) files allowed");
//        }
//
//        Role studentRole = roleRepository.findByName("STUDENT")
//                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));
//
//        int total = 0;
//        int imported = 0;
//        int skipped = 0;
//
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                total++;
//
//                String email = getCell(row, 0);
//                String firstname = getCell(row, 1);
//                String lastname = getCell(row, 2);
//                String regNumber = getCell(row, 3);
//                Department departmentCode = getCell(row, 4);
//                Course courseCode = getCell(row, 5);
//
//                //                String voterId = getCell(row, 3); the voterId if we keep in excel admin can modify so we should just put in backend service
//
//
//
//
//
//
//
//                if (email == null || regNumber == null) {
//                    skipped++;
//                    System.out.println("Skipped because missing email or regNo at row " + row.getRowNum());
//                    continue;
//                }
//
//                if (studentRepository.existsByEmail(email)) {
//                    skipped++;
//                    System.out.println("Skipped because email exists: " + email);
//                    continue;
//                }
//
//                if (studentRepository.existsByRegNumber(regNumber)) {
//                    skipped++;
//                    System.out.println("Skipped because regNo exists: " + regNumber);
//                    continue;
//                }
//
//                String voterId = generateUniqueVoterId();
//                Student student = Student.builder()
//                        .email(email)
//                        .firstname(firstname)
//                        .lastname(lastname)
//                        .regNumber(regNumber)
//                        .voterId(voterId)
//                        .department(departmentCode)
//                        .course(courseCode)
//                        .enabled(false)
//                        .role(studentRole)
//                        .build();
//
//                studentRepository.save(student);
//                imported++;
//            }
//
//            return new ImportResultResponse(total, imported, skipped);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Student import failed", e);
//        }
//    }
//    // method to generate the unique voterId and it should be private
//    private String generateUniqueVoterId() {
//        String voterId;
//        do {
//            voterId = "VOT-" + UUID.randomUUID()
//                    .toString()
//                    .substring(0, 8)
//                    .toUpperCase();
//        } while (studentRepository.existsByVoterId(voterId));
//        return voterId;
//    }
//
//
//    private String getCell(Row row, int index) {
//        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
//        return cell == null ? null : cell.getStringCellValue().trim();
//    }
//    @Transactional
//    public void createStudent(StudentCreateRequest request) {
//
//        Department department = departmentRepository
//                .findByDepartmentCode(request.getDepartmentCode())
//                .orElseThrow(() ->
//                        new IllegalArgumentException("Invalid department"));
//
//        Course course = courseRepository
//                .findByCourseCode(request.getCourseCode())
//                .orElseThrow(() ->
//                        new IllegalArgumentException("Invalid course"));
//
//        // 🚨 Business rule validation
//        if (!course.getDepartment().getId().equals(department.getId())) {
//            throw new IllegalArgumentException(
//                    "Course does not belong to department"
//            );
//        }
//
//        Student student = Student.builder()
//                .email(request.getEmail())
//                .firstname(request.getFirstname())
//                .regNumber(request.getRegNumber())
//                .department(department.getDepartmentCode())
//                .course(course.getCourseCode())
//                .enabled(false)
//                .build();
//
//        studentRepository.save(student);
//    }
//}
//
//
//
//
//
//todo old method of generating the excel
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // header
//
//                total++;
//
//                String email = getCell(row, 0);
//                String firstname = getCell(row, 1);
//                String lastname = getCell(row, 2);
//                String regNo = getCell(row, 3);
//                String departmentCode = getCell(row, 4); // CSE / ETE
//                String courseCode = getCell(row, 5);     // CS / EE / CE
//                String mail = getCell(row, 6);
//                Integer Study_Year = getCell(row, 7);
//
//                if (email == null || regNo == null
//                        || departmentCode == null || courseCode == null || mail == null ) {
//                    skipped++;
//                    continue;
//                }
//
//                if (studentRepository.existsByEmail(email)
//                        || studentRepository.existsByRegNumber(regNo)) {
//                    skipped++;
//                    continue;
//                }
//
//                // 🔍 Fetch Department
//                Department department = departmentRepository
//                        .findByDepartmentCode(departmentCode)
//                        .orElseThrow(() ->
//                                new IllegalArgumentException("Invalid department: " + departmentCode));
//
//                // 🔍 Fetch Course
//                Course course = courseRepository
//                        .findByCourseCode(courseCode)
//                        .orElseThrow(() ->
//                                new IllegalArgumentException("Invalid course: " + courseCode));
//
//                // 🚨 Validate course belongs to department
//                if (!course.getDepartment().getId().equals(department.getId())) {
//                    skipped++;
//                    continue;
//                }
//
//                String voterId = generateUniqueVoterId();
//
//                Student student = Student.builder()
//                        .email(email)
//                        .firstname(firstname)
//                        .lastname(lastname)
//                        .regNumber(regNo)
//                        .voterId(voterId)
//                        .department(department)   // ✅ ENTITY
//                        .course(course)           // ✅ ENTITY
//                        .mail(mail)
//                        .Study_Year(Study_Year)
//                        .enabled(false)
//                        .role(studentRole)
//                        .build();
//
//                studentRepository.save(student);
//                imported++;
//            }
