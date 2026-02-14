package com.yudhassif.election.config;

import com.yudhassif.election.entity.Student;
import com.yudhassif.election.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /* =========================
       EXTRACTION
       ========================= */
//    public Long extractUserId(String token) {
//        Claims claims = extractAllClaims(token);
//
//        if ("ADMIN".equals(claims.get("userType"))) {
//            return claims.get("userId", Long.class);
//        }
//
//        if ("STUDENT".equals(claims.get("userType"))) {
//            return claims.get("studentId", Long.class);
//        }
//
//        throw new IllegalStateException("Unknown user type in token");
//    }
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);

        String userType = claims.get("userType", String.class);

        if ("ADMIN".equals(userType)) return claims.get("userId", Long.class);
        if ("STUDENT".equals(userType)) return claims.get("studentId", Long.class);

        throw new IllegalStateException("Unknown userType in token");
    }



    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    /* =========================
       TOKEN CREATION
       ========================= */

    // 🔹 Access token with full claims
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = buildAccessTokenClaims(userDetails);
        return buildToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    // 🔹 Refresh token (minimal claims)
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = buildRefreshTokenClaims(userDetails);
        return buildToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    // 🔹 Unified token builder
    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* =========================
       CLAIMS BUILDERS
       ========================= */

    private Map<String, Object> buildAccessTokenClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Roles / authorities
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("authorities", authorities);

        // Domain-specific claims
        if (userDetails instanceof User admin) {
            claims.put("userType", "ADMIN");
            claims.put("userId", admin.getId());
            claims.put("email", admin.getEmail());
        } else if (userDetails instanceof Student student) {
            claims.put("userType", "STUDENT");
            claims.put("studentId", student.getId());
            claims.put("regNumber", student.getRegNumber());
        }

        claims.put("tokenType", "ACCESS");
        return claims;
    }

    private Map<String, Object> buildRefreshTokenClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("authorities", authorities);

//        if (userDetails instanceof User admin) {
//            claims.put("userType", "ADMIN");
//            claims.put("userId", admin.getId());
//        } else if (userDetails instanceof Student student) {
//            claims.put("userType", "STUDENT");
//            claims.put("studentId", student.getId());
//        }
        if (userDetails instanceof User admin) {
            claims.put("userType", "ADMIN");
            claims.put("userId", admin.getId());
            claims.put("email", admin.getEmail());
        } else if (userDetails instanceof Student student) {
            claims.put("userType", "STUDENT");
            claims.put("studentId", student.getId());
            claims.put("mail", student.getMail()); // important for username
        }


        claims.put("tokenType", "REFRESH");
        return claims;
    }

    /* =========================
       VALIDATION
       ========================= */

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }
}




























//package com.yudhassif.election.config;
//
//import com.yudhassif.election.entity.Student;
//import com.yudhassif.election.entity.User;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//public class JwtService {
//
//    @Value("${application.security.jwt.secret-key}")
//    private String secretKey;
//
//    @Value("${application.security.jwt.expiration}")
//    private long jwtExpiration;
//
//    @Value("${application.security.jwt.refresh-token.expiration}")
//    private long refreshExpiration;
//
//    /* =========================
//       EXTRACTION
//       ========================= */
//
//    public Long extractUserId(String token) {
//        return Long.parseLong(extractClaim(token, Claims::getSubject));
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
//        return resolver.apply(extractAllClaims(token));
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(
//                Decoders.BASE64.decode(secretKey)
//        );
//    }
//
//    /* =========================
//       TOKEN CREATION
//       ========================= */
//
//    public String generateAccessToken(User user) {
//        return buildAccessTokenClaims(user, jwtExpiration);
//    }
//
//    public String generateRefreshToken(User user) {
//        return buildRefreshTokenClaims(user, refreshExpiration);
//    }
//
////    private String buildToken(User user, long expiration) {
////        return Jwts.builder()
////                .setSubject(String.valueOf(user.getId())) // ✅ USER ID ONLY
////                // .claim("roles", user.getRoles()) // OPTIONAL
////                .setIssuedAt(new Date(System.currentTimeMillis()))
////                .setExpiration(new Date(System.currentTimeMillis() + expiration))
////                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
////                .compact();
////    }
//
//    private String buildAccessTokenClaims(UserDetails userDetails, long expiration) {
//
//        Map<String, Object> claims = new HashMap<>();
//
//        // Roles / authorities
//        claims.put("roles", userDetails.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList()
//        );
//
//        // Optional domain-specific claims
//        if (userDetails instanceof User admin) {
//            claims.put("userType", "ADMIN");
//            claims.put("email", admin.getEmail());
//        }
//
//        if (userDetails instanceof Student student) {
//            claims.put("userType", "STUDENT");
//            claims.put("regNumber", student.getRegNumber());
//        }
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    /* =========================
//       VALIDATION
//       ========================= */
//
//    // ✅ JWT validity DOES NOT depend on User
//    public boolean isTokenValid(String token) {
//        return !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractClaim(token, Claims::getExpiration)
//                .before(new Date());
//    }
//}







































//package com.yudhassif.security.config;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.*;
//import java.util.function.Function;
//
//@Slf4j
//@Service
//public class JwtService {
//    @Value("${application.security.jwt.secret-key}")
//    private String secretKey;    // ✅ Extract username (subject)
//    @Value("${application.security.jwt.expiration}")
//    private long jwtExpiration;
//    @Value("${application.security.jwt.refresh-token.expiration}")
//    private  long refreshExpiration;
//    public String extractUserId(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    // ✅ Generic claim extractor or(extract single claim method)
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // ✅ Extract all claims
//    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parserBuilder()
//                .setSigningKey(getSignInKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // ✅ Get signing key
//    private Key getSignInKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    // ✅ Generate token with extra claims
//    public String generateToken
//    (Map<String, Object> extraClaims, UserDetails userDetails) {
//        return buildToken(extraClaims, userDetails, jwtExpiration);
//    }
//       //here i don't need exctra claim like role or permission I just need new jwt refresh token each time when it expired so i use hashmap here
//    public String generateRefreshToken(UserDetails userDetails) {
//        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
//
//    }
//       private String buildToken
//               (Map<String, Object> extraClaims, UserDetails userDetails, long expiration){ // generate token from user entity and not user detail
//        return Jwts
//                .builder()
//                .setClaims(extraClaims)                     // custom claims (roles, permissions, etc.)
//                .setSubject(userDetails.getUsername())      // subject = email/username
//                .setIssuedAt(new Date(System.currentTimeMillis())) // issue time
//                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 10h
//                .signWith(getSignInKey(), SignatureAlgorithm.HS256)    // sign with key
//                .compact();
//
//       }
//
//    // ✅ Generate token without extra claims
//    public String generateToken(UserDetails userDetails) {
//        log.warn("Values .....................{}", userDetails); // in production this should be removed
//        Map<String, Object> extraClaims = new HashMap<>();
//        extraClaims.put("authorities", userDetails.getAuthorities());
//        return generateToken(extraClaims, userDetails);
//    }
//
//    // ✅ Validate token
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//} alibou code

