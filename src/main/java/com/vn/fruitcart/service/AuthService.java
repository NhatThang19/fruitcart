package com.vn.fruitcart.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserRegisterReq;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRegisterReq userRegisterReq) {
        User newUser = new User();
        newUser.setEmail(userRegisterReq.getEmail());
        newUser.setPassword(userRegisterReq.getPassword());
        newUser.setActive(true);
    
        userService.createUser(newUser);
    
        return this.userService.createUser(newUser);
    }
}
