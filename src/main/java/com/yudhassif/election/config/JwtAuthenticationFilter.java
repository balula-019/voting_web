package com.yudhassif.election.config;

import com.yudhassif.election.services.CustomUserDetailsService;
import com.yudhassif.election.token.TokenHasher;
import com.yudhassif.election.token.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final TokenHasher tokenHasher; // ✅ Added
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (!isJwtWellFormed(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // 1️⃣ Validate signature & expiration
            if (!jwtService.isTokenValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2️⃣ Ensure it's ACCESS token
            String tokenType = jwtService.extractClaim(
                    jwt,
                    claims -> claims.get("tokenType", String.class)
            );

            if (!"ACCESS".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3️⃣ Extract userId
            Long userId = jwtService.extractUserId(jwt);

            if (userId != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // 4️⃣ Load user directly
                UserDetails userDetails =
                        userDetailsService.loadUserByUserId(userId);

                // 5️⃣ Create authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception ex) {
            log.warn("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    private boolean isJwtWellFormed(String token) {
        return token != null &&
                token.chars().filter(c -> c == '.').count() == 2;
    }
}


//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        final String authHeader = request.getHeader("Authorization");
//
//        // 1️⃣ No Authorization header → skip
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        final String jwt = authHeader.substring(7);
//
//        // 2️⃣ JWT format safety check (must have exactly 2 dots)
//        if (!isJwtWellFormed(jwt)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//
//            // 3️⃣ Validate signature & expiration first
//            if (!jwtService.isTokenValid(jwt)) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // 4️⃣ Block non-ACCESS tokens
//            String tokenType = jwtService.extractClaim(
//                    jwt,
//                    claims -> claims.get("tokenType", String.class)
//            );
//
//            if (!"ACCESS".equals(tokenType)) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // 5️⃣ Extract userId
//            Long userId = jwtService.extractUserId(jwt);
//
//            if (userId != null &&
//                    SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                // 6️⃣ Hash raw JWT before DB lookup
//                String hashedToken = tokenHasher.hash(jwt);
//
//                boolean isTokenValidInDb =
//                        tokenRepository.existsByTokenHashAndExpiredFalseAndRevokedFalse(hashedToken);
//
//                if (!isTokenValidInDb) {
//                    filterChain.doFilter(request, response);
//                    return;
//                }
//
//                // 7️⃣ Load user
//                UserDetails userDetails =
//                        userDetailsService.loadUserByUserId(userId);
//
//                // 8️⃣ Authenticate
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                userDetails.getAuthorities()
//                        );
//
//                authToken.setDetails(
//                        new WebAuthenticationDetailsSource().buildDetails(request)
//                );
//
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//
//        } catch (Exception ex) {
//            log.warn("JWT authentication failed: {}", ex.getMessage());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//


