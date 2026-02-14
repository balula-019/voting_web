package com.yudhassif.election.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        // ADMIN FULL ACCESS
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/student/**")
                        .hasRole("STUDENT")

                        // EVERYTHING ELSE BLOCKED (for now)
                        .anyRequest().authenticated()
                );

        return http.build();
    }// add logout functionality
}







//package com.yudhassif.election.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.logout.LogoutHandler;
//
//import static org.springframework.http.HttpMethod.*;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//@EnableMethodSecurity
//public class SecurityConfiguration {
//
//    private final AuthenticationProvider authenticationProvider;
//    private final JwtAuthenticationFilter jwtAuthFilter;
//    private final LogoutHandler logoutHandler;
//
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf().disable()
//
//                .authorizeHttpRequests()
//                .requestMatchers(
//                        "/api/v1/auth/**"
//                ).permitAll()
//
//                .requestMatchers("/api/v1/management/**")
//                .hasAnyRole("ADMIN", "STUDENT")
//
//                .requestMatchers(GET, "/api/v1/management/**")
//                .hasAnyAuthority("ADMIN_READ", "MANAGER_READ")
//
//                .requestMatchers(POST, "/api/v1/management/**")
//                .hasAnyAuthority("ADMIN_CREATE", "MANAGER_CREATE")
//
//                .requestMatchers(PUT, "/api/v1/management/**")
//                .hasAnyAuthority("ADMIN_UPDATE", "MANAGER_UPDATE")
//
//                .requestMatchers(DELETE, "/api/v1/management/**")
//                .hasAnyAuthority("ADMIN_DELETE", "MANAGER_DELETE")
//
//                .anyRequest()
//                .authenticated()
//                .and()
//
//                // 🔹 OAuth2 login (Google/GitHub)
//
//                // 🔹 JWT is still STATELESS
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // allow session for only for oauth login other normal login will use stateless
//                )
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // jwt filter here is globally so we need to exclude oauth from jwt service
//
//                .logout()
//                .logoutUrl("/api/v1/auth/logout")
//                .addLogoutHandler(logoutHandler)
//                .logoutSuccessHandler(
//                        (request, response, authentication) ->
//                                SecurityContextHolder.clearContext()
//                );
//
//        return http.build();
//    }
//}


























































//package com.yudhassif.security.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.logout.LogoutHandler;
//import static org.springframework.http.HttpMethod.*;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//@EnableMethodSecurity
//public class SecurityConfiguration {
//    private final  AuthenticationProvider authenticationProvider;
//
//    private final JwtAuthenticationFilter  jwtAuthFilter;
//    private final LogoutHandler logoutHandler;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        http
//                .csrf()
//                .disable()
//                .authorizeHttpRequests()
//                .requestMatchers(
//                        "/api/v1/auth/**",
//                        "/oauth2/**",
//                        "/login/oauth2/**"
//                ).permitAll()
//
//                .requestMatchers("/api/v1/management/**").hasAnyRole("ADMIN", "MANAGER")
//                .requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority("ADMIN_READ", "MANAGER_READ")
//                .requestMatchers(POST, "/api/v1/management/**").hasAnyAuthority("ADMIN_CREATE", "MANAGER_CREATE")
//                .requestMatchers(DELETE, "/api/v1/management/**").hasAnyAuthority("ADMIN_DELETE", "MANAGER_DELETE")
//                .requestMatchers(PUT, "/api/v1/management/**").hasAnyAuthority("ADMIN_UPDATE", "MANAGER_UPDATE")
//
////                .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())
////                .requestMatchers("/api/v1/admin/**").hasAuthority(ADMIN_CREATE)
////                .requestMatchers("/api/v1/admin/**").hasAuthority(ADMIN_READ)
////                .requestMatchers("/api/v1/admin/**").hasAuthority(ADMIN_DELETE)
////                .requestMatchers("/api/v1/admin/**").hasAuthority(ADMIN_UPDATE)
////
//                .anyRequest()
//                .authenticated()
//                .and()
//                //required to be learn myself
//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(userInfo ->
//                                userInfo.userService(customOAuth2UserService)
//                        )
//                        .successHandler(oAuth2SuccessHandler)
//                )
//
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                //should i know who is log in through authentication provider
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .logout()
//                .logoutUrl("/api/v1/auth/logout")
//                .addLogoutHandler(logoutHandler)
//                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());
//
//        return http.build();
//
//    }
//}

