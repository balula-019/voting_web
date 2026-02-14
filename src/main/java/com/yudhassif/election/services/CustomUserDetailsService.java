package com.yudhassif.election.services;

import com.yudhassif.election.Student.StudentRepository;
import com.yudhassif.election.entity.Student;
import com.yudhassif.election.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        // 1️⃣ Try to load as admin first (by User.email)
        return userRepository.findByEmail(identifier)
                .or(() -> {
                    // 2️⃣ If not found, try student mail
                    return studentRepository.findByMail(identifier)
                            .map(Student::getUser); // get the associated User
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // ✅ Load by userId (for JWT refresh etc.)
    public UserDetails loadUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}































//package com.yudhassif.election.services;
//
//import com.yudhassif.election.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
////    @Override
////    public UserDetails loadUserByUsername(String username) {
////        return userRepository.findByEmail(username)
////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////    }
//@Override
//public UserDetails loadUserByUsername(String identifier) {
//    return userRepository.findByEmail(identifier)
//            .or(() -> userRepository.findByMail(identifier)) // try student mail if email not found
//            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//}
//
//
//    // ✅ CUSTOM method (NOT from interface)
//    public  UserDetails loadUserByUserId(Long userId) {
//        return userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }
//}
