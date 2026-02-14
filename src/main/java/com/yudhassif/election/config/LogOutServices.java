package com.yudhassif.election.config;

import com.yudhassif.election.token.Token;
import com.yudhassif.election.token.TokenHasher;
import com.yudhassif.election.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogOutServices implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final TokenHasher tokenHasher; // ✅ add this

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final String jwt = authHeader.substring(7);

        // ✅ Hash raw token before DB lookup
        String hashedToken = tokenHasher.hash(jwt);

        Token storedToken = tokenRepository
                .findByTokenHash(hashedToken)
                .orElse(null);

        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}






































//package com.yudhassif.election.config;
//
//import com.yudhassif.election.token.TokenRepository;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.logout.LogoutHandler;
//import org.springframework.stereotype.Service;
//
//
//
//@Service
//@RequiredArgsConstructor
//public class LogOutServices implements LogoutHandler {
//    private final TokenRepository tokenRepository;
//    @Override
//    public void logout(HttpServletRequest request,
//                       HttpServletResponse response,
//                       Authentication authentication) {
//        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
//        //chek JWT token
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) { // so here is null and not start with bearer word then solution is to continue to other filter chain up to get security gad that can generate a token
//            return;
//        }
//        jwt = authHeader.substring(7);
//        // extract the pure token by doing substring at index 7 from plain java
//        var storedToken = tokenRepository.findByTokenHashed(jwt)
//                .orElse(null);
//        if (storedToken != null){
//            storedToken.setExpired(true);
//            storedToken.setRevoked(true);
//            tokenRepository.save(storedToken);
//        }
//        // the above method first it check the token in to the database, if it there ,after user log out we implement storedToken to be expired and revoked
//
//    }
//}
