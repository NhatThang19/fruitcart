package com.vn.fruitcart.service;

import java.util.List;
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

    public Optional<Role> findRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vai trò người dùng: " + name));
    }

    public void save(Role role) {
        roleRepository.save(role);
    }

    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
