package com.vn.fruitcart.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.RegisterReq;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterReq rReq) throws Exception {
        User newUser = User.builder()
                .lastName(rReq.getLastName())
                .firstName(rReq.getFirstName())
                .email(rReq.getEmail())
                .phone(rReq.getPhone())
                .address(rReq.getAddress())
                .password(passwordEncoder.encode(rReq.getPassword()))
                .isBlocked(false)
                .avatarUrl("/storage/default/avatar.jpg")
                .build();

        Role userRole = roleService.getRoleByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò người dùng."));
        newUser.setRole(userRole);

        userService.save(newUser);
    }
}
