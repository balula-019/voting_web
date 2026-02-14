package com.yudhassif.election.seeder;

import com.yudhassif.election.entity.User;
import com.yudhassif.election.repository.UserRepository;
import com.yudhassif.election.role.Role;
import com.yudhassif.election.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1️⃣ Check if any ADMIN already exists
        boolean adminExists = userRepository.existsByRole_Name("ADMIN");
        if (adminExists) {
            log.info("Admin already exists. Skipping bootstrap admin creation.");
            return;
        }

        // 2️⃣ Fetch ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() ->
                        new IllegalStateException("ADMIN role not found. Seed roles first.")
                );

        // 3️⃣ Create bootstrap admin-owner of the system
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Yudhassif");
        admin.setEmail("bayula228@gmail.com");     // admin@election.ac.tz
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(adminRole);
        admin.setEnabled(true);

        userRepository.save(admin);

        log.info("✅ Bootstrap admin created: admin@election.ac.tz");   //
    }
}
// seeder class it automatically import if its admin import admin data automatically
// todo to test using postman importing of excel and see the response fajr in shaa allah
// todo to logout and testing