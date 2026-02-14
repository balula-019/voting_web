package com.yudhassif.election.repository;


import com.yudhassif.election.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    boolean existsByNameAndRole_Name(String perm, String name);
}
