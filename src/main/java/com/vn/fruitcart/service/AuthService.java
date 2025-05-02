package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.RegisterReq;

@Service
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;

    public AuthService(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    public User register(RegisterReq userRegisterReq) {
        User newUser = new User();
        newUser.setUsername(userRegisterReq.getUsername());
        newUser.setEmail(userRegisterReq.getEmail());
        newUser.setPassword(userRegisterReq.getPassword());
        newUser.setAvatar("/storage/images/avatar.jpeg");
        newUser.setActive(true);
        newUser.setRole(this.roleService.getRoleByName("USER"));

        return userService.createUser(newUser);
    }
}
