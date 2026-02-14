package com.yudhassif.election.services;

import com.yudhassif.election.entity.Permission;
import com.yudhassif.election.repository.PermissionRepository;
import com.yudhassif.election.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PermissionService{

    private final PermissionRepository permissionRepository;
    public Permission createPermission(String name, Role role,long id) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                                .id(id)
                                .name(name)
                                .role(role)
                        .build()));
    }

    public Optional<Permission> getByName(String name) {
        return permissionRepository.findByName(name);
    }
}

