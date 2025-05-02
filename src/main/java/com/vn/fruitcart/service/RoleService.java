package com.vn.fruitcart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Optional<Role> getRoleById(Long id) {
        return this.roleRepository.findById(id);
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findRoleByName(name);
    }
}
