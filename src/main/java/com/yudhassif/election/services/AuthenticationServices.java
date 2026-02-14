package com.yudhassif.election.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.config.JwtService;
import com.yudhassif.election.entity.Student;
import com.yudhassif.election.entity.User;
import com.yudhassif.election.repository.UserRepository;
import com.yudhassif.election.request.ActivateAccountRequest;
import com.yudhassif.election.request.AdminLoginRequest;
import com.yudhassif.election.response.AuthenticationResponse;
import com.yudhassif.election.token.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServices {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final ActivationTokenRepository repository;
    private final TokenHasher tokenHasher;

    /* =========================
       HELPER METHODS
       ========================= */



    private void revokeAllUserToken(User user) {
        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    /* =========================
       LOGIN (ADMIN / STUDENT)
       ========================= */

//    @Transactional
//    public AuthenticationResponse login(AdminLoginRequest request) {
//
//        // 1️⃣ Authenticate credentials
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.identifier(),
//                            request.password()
//                    )
//            );
//        } catch (BadCredentialsException ex) {
//            throw new BadCredentialsException("Invalid credentials or account not activated");
//        }
//
//        // 2️⃣ Load user details
//        UserDetails userDetails = userDetailsService.loadUserByUsername(request.identifier());
//
//        // 3️⃣ Generate JWT tokens
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//
//        // 4️⃣ Save tokens for **all users**, not just admins
//        User user = userRepository.findByEmail(request.identifier())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // Ensure account is enabled/activated
//        if (!user.isEnabled()) {
//            throw new BadCredentialsException("Account not activated");
//        }
//
//        revokeAllUserToken(user);
//        saveUserToken(user, accessToken);
//
//        return AuthenticationResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken)
//                .message("Login successful")
//                .build();
//    }
//    @Transactional
@Transactional
public AuthenticationResponse login(AdminLoginRequest request) {

    String identifier = request.identifier();
    String password = request.password();

    // 1️⃣ Determine the User entity (admin or student)
    User user = resolveUserByIdentifier(identifier);

    // 2️⃣ Authenticate using User email + password
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),   // ALWAYS authenticate using User email
                        password
                )
        );
    } catch (BadCredentialsException ex) {
        throw new BadCredentialsException("Invalid credentials");
    }

    // 3️⃣ Ensure account is enabled
    if (!user.isEnabled()) {
        throw new BadCredentialsException("Account not activated");
    }

    // 4️⃣ Generate tokens (STATELESS ACCESS TOKEN)
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // 5️⃣ Store ONLY refresh token
    saveRefreshToken(user, refreshToken);

    return AuthenticationResponse.builder()
            .access_token(accessToken)
            .refresh_token(refreshToken)
            .message("Login successful")
            .build();
}
    private User resolveUserByIdentifier(String identifier) {

        // 1️⃣ Try admin login (User.email)
        return userRepository.findByEmail(identifier)

                // 2️⃣ If not admin, try student mail
                .orElseGet(() ->
                        studentRepository.findByMail(identifier)
                                .map(Student::getUser)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException("User not found")
                                )
                );
    }
    private void saveRefreshToken(User user, String refreshToken) {

        String tokenHash = tokenHasher.hash(refreshToken);

        Token token = Token.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }




    /* =========================
       REFRESH TOKEN
       ========================= */

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String refreshToken = authHeader.substring(7);

        // 1️⃣ Validate JWT structure & expiration
        if (!jwtService.isTokenValid(refreshToken)) {
            return;
        }

        // 2️⃣ Extract user ID
        Long userId = jwtService.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3️⃣ Verify token exists in DB (hashed)
        String tokenHash = tokenHasher.hash(refreshToken);

        Token storedToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token is revoked");
        }

        // 4️⃣ Generate new access token (STATELESS)
        String newAccessToken = jwtService.generateAccessToken(user);

        // 5️⃣ (Optional but recommended) Rotate refresh token
        storedToken.setRevoked(true);
        storedToken.setExpired(true);
        tokenRepository.save(storedToken);

        String newRefreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, newRefreshToken);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .access_token(newAccessToken)
                .refresh_token(newRefreshToken)
                .build();

        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }


    /* =========================
       STUDENT ACTIVATION
       ========================= */

    @Transactional
    public void activateStudent(ActivateAccountRequest request) {

        // 1️⃣ Find activation token
        ActivationToken token = repository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (token.isUsed()) throw new IllegalStateException("Token already used");
        if (token.getExpiresAt().isBefore(Instant.now())) throw new IllegalStateException("Token expired");

        // 2️⃣ Validate passwords
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");

        // 3️⃣ Get user from token
        User user = token.getUser();

        // 4️⃣ Activate account
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setEnabled(true);

        // 5️⃣ Update student profile
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Student profile not found"));
        student.setActivated(true);

        // 6️⃣ Mark token as used
        token.setUsed(true);

        // 7️⃣ Save changes
        userRepository.save(user);
        studentRepository.save(student);
        repository.save(token);
    }
}














































































//package com.yudhassif.election.services;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.yudhassif.election.Student.StudentRepository;
//import com.yudhassif.election.config.JwtService;
//import com.yudhassif.election.entity.Student;
//import com.yudhassif.election.entity.User;
//import com.yudhassif.election.repository.UserRepository;
//import com.yudhassif.election.request.ActivateAccountRequest;
//import com.yudhassif.election.request.AdminLoginRequest;
//import com.yudhassif.election.response.AuthenticationResponse;
//import com.yudhassif.election.token.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.time.Instant;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthenticationServices {
//
//    private final TokenRepository tokenRepository;
//    private final UserRepository userRepository;
//    private final StudentRepository studentRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//    private final CustomUserDetailsService userDetailsService;
//    private final ActivationTokenRepository activationTokenRepository;
//    private final TokenHasher tokenHasher;
//
//    /* =========================
//       TOKEN HELPERS
//       ========================= */
//
//    private void saveUserToken(User user, String jwtToken) {
//        String tokenHash = tokenHasher.hash(jwtToken);
//        Token token = Token.builder()
//                .user(user)
//                .tokenHash(tokenHash)
//                .tokenType(TokenType.BEARER)
//                .expired(false)
//                .revoked(false)
//                .build();
//        tokenRepository.save(token);
//    }
//
//    private void revokeAllUserTokens(User user) {
//        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
//        if (validTokens.isEmpty()) return;
//        validTokens.forEach(t -> {
//            t.setExpired(true);
//            t.setRevoked(true);
//        });
//        tokenRepository.saveAll(validTokens);
//    }
//
//    /* =========================
//       LOGIN (ADMIN / STUDENT)
//       ========================= */
//
//    @Transactional
//    public AuthenticationResponse login(AdminLoginRequest request) {
//        // 1️⃣ Authenticate using Spring Security
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.identifier(),
//                            request.password()
//                    )
//            );
//        } catch (BadCredentialsException ex) {
//            throw new BadCredentialsException("Invalid credentials or account not activated");
//        }
//
//        // 2️⃣ Load user details
//        UserDetails userDetails = userDetailsService.loadUserByUsername(request.identifier());
//
//        // 3️⃣ Generate JWT tokens
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//
//        // 4️⃣ Save tokens for **everyone**, not just admin
//        User user = userRepository.findByEmail(request.identifier())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        revokeAllUserTokens(user);
//        saveUserToken(user, accessToken);
//
//        return AuthenticationResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken)
//                .message("Login successful")
//                .build();
//    }
//
//    /* =========================
//       REFRESH TOKEN
//       ========================= */
//
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;
//
//        String refreshToken = authHeader.substring(7);
//        Long userId = jwtService.extractUserId(refreshToken);
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (jwtService.isTokenValid(refreshToken)) {
//            String newAccessToken = jwtService.generateAccessToken(user);
//
//            // Save new access token for everyone
//            revokeAllUserTokens(user);
//            saveUserToken(user, newAccessToken);
//
//            AuthenticationResponse authResponse = AuthenticationResponse.builder()
//                    .access_token(newAccessToken)
//                    .refresh_token(refreshToken)
//                    .build();
//
//            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
//        }
//    }
//
//    /* =========================
//       STUDENT ACTIVATION
//       ========================= */
//
//    @Transactional
//    public void activateStudent(ActivateAccountRequest request) {
//        // 1️⃣ Find activation token
//        ActivationToken token = activationTokenRepository.findByToken(request.token())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
//
//        if (token.isUsed()) throw new IllegalStateException("Token already used");
//        if (token.getExpiresAt().isBefore(Instant.now())) throw new IllegalStateException("Token expired");
//
//        // 2️⃣ Validate passwords
//        if (!request.newPassword().equals(request.confirmPassword()))
//            throw new IllegalArgumentException("Passwords do not match");
//
//        // 3️⃣ Get user from token
//        User user = token.getUser();
//
//        // 4️⃣ Activate account
//        user.setPassword(passwordEncoder.encode(request.newPassword()));
//        user.setEnabled(true);
//
//        // 5️⃣ Update student profile
//        Student student = studentRepository.findByUser(user)
//                .orElseThrow(() -> new IllegalStateException("Student profile not found"));
//        student.setActivated(true);
//
//        // 6️⃣ Mark token as used
//        token.setUsed(true);
//
//        // 7️⃣ Save changes
//        userRepository.save(user);
//        studentRepository.save(student);
//        activationTokenRepository.save(token);
//    }
//}
//
//
//
//
////
////
////
////
////
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//package com.yudhassif.election.services;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.yudhassif.election.Student.StudentRepository;
//import com.yudhassif.election.config.JwtService;
//import com.yudhassif.election.entity.Student;
//import com.yudhassif.election.entity.User;
//import com.yudhassif.election.repository.UserRepository;
//import com.yudhassif.election.request.ActivateAccountRequest;
//import com.yudhassif.election.request.AdminLoginRequest;
//import com.yudhassif.election.response.AuthenticationResponse;
//import com.yudhassif.election.role.RoleRepository;
//import com.yudhassif.election.token.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.time.Instant;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthenticationServices {
//
//    private final TokenRepository tokenRepository;
//    private final UserRepository userRepository;
//    private final StudentRepository studentRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//    private final CustomUserDetailsService userDetailsService;
//    private final ActivationTokenRepository repository;
//    private final TokenHasher tokenHasher;
//
//    /* =========================
//       HELPER METHODS
//       ========================= */
//
//    private void saveUserToken(User user, String jwtToken) {
//        String tokenHash = tokenHasher.hash(jwtToken);
//        Token token = Token.builder()
//                .user(user)
//                .tokenHash(tokenHash)
//                .tokenType(TokenType.BEARER)
//                .expired(false)
//                .revoked(false)
//                .build();
//        tokenRepository.save(token);
//    }
//
//    private void revokeAllUserToken(User user) {
//        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
//        if (validTokens.isEmpty()) return;
//        validTokens.forEach(t -> {
//            t.setExpired(true);
//            t.setRevoked(true);
//        });
//        tokenRepository.saveAll(validTokens);
//    }
//
//    /* =========================
//       LOGIN (ADMIN / STUDENT)
//       ========================= */
//
//    @Transactional
//    public AuthenticationResponse login(AdminLoginRequest request) {
//
//        // Authenticate credentials
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.identifier(),
//                            request.password()
//                    )
//            );
//        } catch (BadCredentialsException ex) {
//            throw new BadCredentialsException("Invalid credentials");
//        }
//
//        // Load user details (admin or student)
//        UserDetails userDetails = userDetailsService.loadUserByUsername(request.identifier());
//
//        // Generate JWT tokens using senior-style claims
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//
//        // Save tokens only for admins (optional)
//        if (userDetails instanceof User admin) {
//            revokeAllUserToken(admin);
//            saveUserToken(admin, accessToken);
//        }
//
//        return AuthenticationResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken)
//                .message("Login successful")
//                .build();
//    }
//
//    /* =========================
//       REFRESH TOKEN
//       ========================= */
//
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;
//
//        String refreshToken = authHeader.substring(7);
//        Long userId = jwtService.extractUserId(refreshToken);
//
//        // Load only from UserRepository
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (jwtService.isTokenValid(refreshToken)) {
//            String newAccessToken = jwtService.generateAccessToken(user);
//
//            // Save token only for admins
//            if (user.getRole().getName().equals("ADMIN")) {
//                revokeAllUserToken(user);
//                saveUserToken(user, newAccessToken);
//            }
//
//            AuthenticationResponse authResponse = AuthenticationResponse.builder()
//                    .access_token(newAccessToken)
//                    .refresh_token(refreshToken)
//                    .build();
//
//            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
//        }
//    }
//
//
//    /* =========================
//       STUDENT ACTIVATION
//       ========================= */
//
//    @Transactional
//    public void activateStudent(ActivateAccountRequest request) {
//
//        // 1️⃣ Find token
//        ActivationToken token = repository.findByToken(request.token())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
//
//        // Optional: check if already used
//        if (token.isUsed()) {
//            throw new IllegalStateException("Token already used");
//        }
//
//        // Optional: check expiration
//        if (token.getExpiresAt().isBefore(Instant.now())) {
//            throw new IllegalStateException("Token expired");
//        }
//
//        // 2️⃣ Validate passwords
//        if (!request.newPassword().equals(request.confirmPassword())) {
//            throw new IllegalArgumentException("Passwords do not match");
//        }
//
//        // 3️⃣ Get User directly from token ✅
//        User user = token.getUser();
//
//        // 4️⃣ Get Student profile (if needed)
//        Student student = studentRepository.findByUser(user)
//                .orElseThrow(() -> new IllegalStateException("Student profile not found"));
//
//        // 5️⃣ Activate account
//        user.setPassword(passwordEncoder.encode(request.newPassword()));
//        user.setEnabled(true);
//        student.setActivated(true);
//
//        // 6️⃣ Mark token as used
//        token.setUsed(true);
//
//        // 7️⃣ Save (Transactional handles flush automatically)
//    }
//
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////
////package com.yudhassif.election.services;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.yudhassif.election.Student.StudentRepository;
////import com.yudhassif.election.config.JwtService;
////import com.yudhassif.election.entity.Student;
////import com.yudhassif.election.entity.User;
////import com.yudhassif.election.repository.UserRepository;
////import com.yudhassif.election.request.ActivateAccountRequest;
////import com.yudhassif.election.request.AdminLoginRequest;
////import com.yudhassif.election.response.AuthenticationResponse;
////import com.yudhassif.election.role.RoleRepository;
////import com.yudhassif.election.token.ActivationToken;
////import com.yudhassif.election.token.Token;
////import com.yudhassif.election.token.TokenRepository;
////import com.yudhassif.election.token.TokenType;
////import jakarta.servlet.http.HttpServletRequest;
////import jakarta.servlet.http.HttpServletResponse;
////import jakarta.transaction.Transactional;
////import lombok.*;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.http.HttpHeaders;
////import org.springframework.security.authentication.AuthenticationManager;
////import org.springframework.security.authentication.BadCredentialsException;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.userdetails.UserDetails;
////import org.springframework.security.core.userdetails.UsernameNotFoundException;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.stereotype.Service;
////
////import java.io.IOException;
////import java.time.Instant;
////
////@Slf4j
////@Service
////@RequiredArgsConstructor
////public class AuthenticationServices {
////    private final TokenRepository tokenRepository;
////    private final UserRepository repository;
////    private final PasswordEncoder passwordEncoder;
////    private final JwtService jwtService;
////    private final AuthenticationManager authenticationManager;
////    private final RoleRepository roleRepository;
////    private final StudentRepository repo;
////    private final CustomUserDetailsService userDetailsService;
////
////    /**
////     * Register a new user:
////     * - check if email exists
////     * - encode password
////     * - save to DB
////     * - generate JWT token
////     */
//////typical in a system role assign in the database before anyone register
////
////
////
////    private void saveUserToken(User user, String jwtToken) {
////        var token = Token.builder()
////                .user(user)
////                .token(jwtToken)
////                .tokenType(TokenType.BEARER)
////                .expired(false)
////                .revoked(false)
////                .build();
////        tokenRepository.save(token);
////    }
////    private void revokeAllUserToken(User user){
////        //this check the token that is valid in the database
////        // valid token here means token that are not expired and revoked as the @Query annonation written in token repository
////        var validUserToken = tokenRepository.findAllValidTokensByUser(user.getId());
////        // if the token is expired and revoked do nothing
////        if (validUserToken.isEmpty())
////            return;
////        // for each token which are expired and revoked set to true
////        validUserToken.forEach(t -> {
////                    t.setExpired(true);
////                    t.setRevoked(true);
////                }
////        );
////        tokenRepository.saveAll(validUserToken);
////    }
////
////
////
////
////
////
////    /**
////     * Authenticate existing user:
////     * - validate credentials
////     * - generate JWT token
////     */
////
////
////    @Transactional
////    public AuthenticationResponse login(AdminLoginRequest request) {
////
////        // 1️⃣ Authenticate (Spring Security handles password + enabled checks)
////        try {
////            authenticationManager.authenticate(
////                    new UsernamePasswordAuthenticationToken(
////                            request.identifier(),
////                            request.password()
////                    )
////            );
////        } catch (BadCredentialsException ex) {
////            throw new BadCredentialsException("Invalid credentials");
////        }
////
////        // 2️⃣ Load authenticated user (ADMIN or STUDENT)
////        UserDetails userDetails =
////                userDetailsService.loadUserByUsername(request.identifier());
////
////        // 3️⃣ Generate tokens
////        String accessToken = jwtService.generateAccessToken(userDetails);
////        String refreshToken = jwtService.generateRefreshToken(userDetails);
////
////        // 4️⃣ Save token only for ADMIN (optional)
////        if (userDetails instanceof User admin) {
////            revokeAllUserToken(admin);
////            saveUserToken(admin, accessToken);
////        }
////
////        return AuthenticationResponse.builder()
////                .access_token(accessToken)
////                .refresh_token(refreshToken)
////                .message("Login successful")
////                .build();
////    }
////
//////    @Transactional
//////    public AuthenticationResponse login(AdminLoginRequest request2) {
//////        try {
//////            authenticationManager.authenticate(
//////                    new UsernamePasswordAuthenticationToken(
//////                            request2.identifier(),
//////                            request2.password()
//////                    )
//////            );
//////        } catch (BadCredentialsException ex) {
//////            log.warn("❌ Login failed for email: {}", request2.identifier());
//////            throw new BadCredentialsException("Invalid email or password");
//////        }
//////
//////        var user = repository.findByEmail(request2.identifier())
//////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//////        var jwtToken = jwtService.generateAccessToken(user);
//////        var refreshToken = jwtService.generateRefreshToken(user);
//////
//////        revokeAllUserToken(user);
//////        saveUserToken(user, jwtToken); // ✅ FIXED
//////
//////
//////
//////        // 🔥 Log it so you see in backend console
//////        log.info("✅ Generated Token: {}", jwtToken);
//////
//////
//////        // (Optional) Save token to DB
//////        // user.setToken(jwtToken);
//////        // repository.save(user);
//////        var user1 = repository.findByEmail(request2.identifier())
//////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//////
//////        if (!user1.isEnabled()) {
//////            throw new BadCredentialsException("Account not activated");
//////        }
//////
//////        if (!user1.getRole().getName().equals("ADMIN")) {
//////            throw new BadCredentialsException("Access denied");
//////        }
//////
//////
//////        return AuthenticationResponse.builder()
//////                .access_token(jwtToken)
//////                .refresh_token(refreshToken)
//////                .message("Welcome back")
//////                .build();
//////
//////    }
//////
//////
////
////
////
////
////
////
////
////
////
////    public void refreshToken(
////            HttpServletRequest request,
////            HttpServletResponse response) throws IOException {
////
////        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
////
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            return;
////        }
////
////        final String refreshToken = authHeader.substring(7);
////        Long userId = jwtService.extractUserId(refreshToken);
////
////        var user = repository.findById(userId)
////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////
////        if (jwtService.isTokenValid(refreshToken)) {
////
////            var accessToken = jwtService.generateAccessToken(user);
////
////            revokeAllUserToken(user);
////            saveUserToken(user, accessToken);
////
////            var authResponse = AuthenticationResponse.builder()
////                    .access_token(accessToken)
////                    .refresh_token(refreshToken)
////                    .build();
////
////            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
////        }
////    }
////    @Transactional
////    public void activateStudent(ActivateAccountRequest request) {
////
////        ActivationToken token = tokenRepository.findAll()
////                .stream()
////                .filter(t -> passwordEncoder.matches(
////                        request.token(), t.getTokenHash()))
////                .findFirst()
////                .orElseThrow(() ->
////                        new IllegalArgumentException("Invalid token"));
////
////        if (token.isUsed()) {
////            throw new IllegalStateException("Token already used");
////        }
////
////        if (Instant.now().isAfter(token.getExpiresAt())) {
////            throw new IllegalStateException("Token expired");
////        }
////
////        if (!request.newPassword().equals(request.confirmPassword())) {
////            throw new IllegalArgumentException("Passwords do not match");
////        }
////
////        Student student = token.getStudent();
////        student.setPassword(passwordEncoder.encode(request.newPassword()));
////        student.setActivated(true);
////
////        token.setUsed(true);
////
////        repo.save(student);
////        tokenRepository.save(token);
////    }
////
//////    @Transactional
//////    public void activateStudent(StudentActivationRequest request) {   // done
//////        // Step 1: fetch by email
//////        Student student = repo.findByMail(request.getMail())
//////                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
//////
//////        // Step 2: validate regNo
//////        if (!student.getRegNumber().equals(request.getRegNumber())) {
//////            throw new StudentNotFoundException("Student not found");
//////        }
//////
//////        // Step 3: check activation status
//////        if (student.isActivated()) {
//////            throw new AlreadyActivatedException("Account is already activated");
//////        }
//////
//////        // Step 4: validate initial password (encoded version)
//////        if (!passwordEncoder.matches("Bayula@2026", student.getPassword())) {
//////            throw new PasswordNotFoundException("Incorrect initial password");
//////        }
//////
//////        // Step 5: validate new password match
//////        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//////            throw new PasswordNotFoundException("Password did not match");
//////        }
//////
//////        // Step 6: encode and set new password, activate account
//////        student.setPassword(passwordEncoder.encode(request.getNewPassword()));
//////        student.setActivated(true);
//////
//////        // Save updated student
//////        repo.save(student);
//////    }
////
////}
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
//////package com.yudhassif.election.services;
//////
//////import com.fasterxml.jackson.databind.ObjectMapper;
//////import com.yudhassif.election.config.JwtService;
//////import com.yudhassif.election.entity.User;
//////import com.yudhassif.election.repository.UserRepository;
//////import com.yudhassif.election.request.AuthenticationRequest;
//////import com.yudhassif.election.response.AuthenticationResponse;
//////import com.yudhassif.election.role.RoleRepository;
//////import com.yudhassif.election.token.Token;
//////import com.yudhassif.election.token.TokenRepository;
//////import com.yudhassif.election.token.TokenType;
//////import jakarta.servlet.http.HttpServletRequest;
//////import jakarta.servlet.http.HttpServletResponse;
//////import jakarta.validation.Valid;
//////import lombok.*;
//////import lombok.extern.slf4j.Slf4j;
//////import org.springframework.http.HttpHeaders;
//////import org.springframework.security.authentication.AuthenticationManager;
//////import org.springframework.security.authentication.BadCredentialsException;
//////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//////import org.springframework.security.core.userdetails.UsernameNotFoundException;
//////import org.springframework.security.crypto.password.PasswordEncoder;
//////import org.springframework.stereotype.Service;
//////
//////import java.io.IOException;
//////@Slf4j
//////@Service
//////@RequiredArgsConstructor
//////@Builder
//////public class AuthenticationServices {
//////    private final TokenRepository tokenRepository;
//////    private final UserRepository repository;
//////    private final PasswordEncoder passwordEncoder;
//////    private final JwtService jwtService;
//////    private final AuthenticationManager authenticationManager;
//////    private final RoleRepository roleRepository;
//////
//////    /**
//////     * Register a new user:
//////     * - check if email exists
//////     * - encode password
//////     * - save to DB
//////     * - generate JWT token
//////     */
////////typical in a system role assign in the database before anyone register
//////
//////
//////    private void saveUserToken(User user, String jwtToken) {
//////        var token = Token.builder()
//////                .user(user)
//////                .token(jwtToken)
//////                .tokenType(TokenType.BEARER)
//////                .expired(false)
//////                .revoked(false)
//////                .build();
//////        tokenRepository.save(token);
//////    }
//////    private void revokeAllUserToken(User user){
//////        //this check the token that is valid in the database
//////        // valid token here means token that are not expired and revoked as the @Query annonation written in token repository
//////        var validUserToken = tokenRepository.findAllValidTokensByUser(user.getId());
//////        // if the token is expired and revoked do nothing
//////        if (validUserToken.isEmpty())
//////            return;
//////        // for each token which are expired and revoked set to true
//////        validUserToken.forEach(t -> {
//////                    t.setExpired(true);
//////                    t.setRevoked(true);
//////                }
//////        );
//////        tokenRepository.saveAll(validUserToken);
//////    }
//////
//////
//////
//////
//////
//////
//////    /**
//////     * Authenticate existing user:
//////     * - validate credentials
//////     * - generate JWT token
//////     */
//////    public AuthenticationResponse login(@Valid AuthenticationRequest request2) {
//////        try {
//////            authenticationManager.authenticate(
//////                    new UsernamePasswordAuthenticationToken(
//////                            request2.getEmail(),
//////                            request2.getPassword()
//////                    )
//////            );
//////        } catch (BadCredentialsException ex) {
//////            log.warn("❌ Login failed for email: {}", request2.getEmail());
//////            throw new BadCredentialsException("Invalid email or password");
//////        }
//////
//////        var user = repository.findByEmail(request2.getEmail())
//////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//////        var jwtToken = jwtService.generateAccessToken(user);
//////        var refreshToken = jwtService.generateRefreshToken(user);
//////
//////        revokeAllUserToken(user);
//////        saveUserToken(user, jwtToken); // ✅ FIXED
//////
//////
//////
//////        // 🔥 Log it so you see in backend console
//////        log.info("✅ Generated Token: {}", jwtToken);
//////
//////        // (Optional) Save token to DB
//////        // user.setToken(jwtToken);
//////        // repository.save(user);
//////
//////        return AuthenticationResponse.builder()
//////                .access_token(jwtToken)
//////                .refresh_token(refreshToken)
//////                .message("Welcome back")
//////                .build();
//////    }
//////    public void refreshToken(
//////            HttpServletRequest request,
//////            HttpServletResponse response) throws IOException {
//////
//////        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//////
//////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//////            return;
//////        }
//////
//////        final String refreshToken = authHeader.substring(7);
//////        Long userId = jwtService.extractUserId(refreshToken);
//////
//////        var user = repository.findById(userId)
//////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//////
//////        if (jwtService.isTokenValid(refreshToken)) {
//////
//////            var accessToken = jwtService.generateAccessToken(user);
//////
//////            revokeAllUserToken(user);
//////            saveUserToken(user, accessToken);
//////
//////            var authResponse = AuthenticationResponse.builder()
//////                    .access_token(accessToken)
//////                    .refresh_token(refreshToken)
//////                    .build();
//////
//////            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
//////        }
//////    }
//////
//////}
