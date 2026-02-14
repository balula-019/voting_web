package com.yudhassif.election.seeder;

import com.yudhassif.election.entity.Permission;
import com.yudhassif.election.repository.PermissionRepository;
import com.yudhassif.election.role.Role;
import com.yudhassif.election.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role is  missing"));
        Role userRole = roleRepository.findByName("STUDENT").orElseThrow(()-> new IllegalStateException("Student role is missing"));

        createPermission("STUDENT_IMPORT", adminRole);// done
        createPermission("STUDENT_CREATE", adminRole); // done
        createPermission("ELECTION_CREATE", adminRole); // done
        createPermission("ELECTION_OPEN", adminRole); // done
        createPermission("RESULT_PUBLISH",adminRole);// later on I will implement after move to student side
        createPermission("STUDENT_UPDATE",adminRole); // remain testing
        createPermission("MANAGE_ELECTION", adminRole); // is not implemented
        createPermission("STUDENT_DELETE", adminRole); // done
        createPermission("CLOSE_ELECTION", adminRole); // done
        createPermission("RESUME_ELECTION", adminRole); // remain testing
        createPermission("SUSPEND_ELECTION", adminRole); // remain testing mannual
        createPermission("ELECTION_UPDATE", adminRole); //done
        createPermission("ELECTION_DELETE", adminRole); // done



        createPermission("VOTE_CAST", userRole); // todo to ensure student vote
        createPermission("RESULT_VIEW",userRole);
        createPermission("DASHBOARD_VIEW", userRole);

    }
// STUDENT_DELETE,CLOSE_ELECTION,ELECTION_OPEN,STUDENT_DELETE,STUDENT_UPDATE,RESUME_ELECTION,CLOSE_ELECTION,SUSPEND_ELECTION,ELECTION_CREATE

    private void createPermission(String name, Role role) {
        if (permissionRepository.findByName(name).isEmpty()) {
            permissionRepository.save(
                    Permission.builder()
                            .name(name)
                            .role(role)
                            .build()
            );
        }
    }
}





