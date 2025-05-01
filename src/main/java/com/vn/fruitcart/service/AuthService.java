package com.vn.fruitcart.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserRegisterReq;
import com.vn.fruitcart.entity.dto.response.ResponseMessage;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseMessage register(UserRegisterReq userRegisterReq) {
        User newUser = new User();
        newUser.setEmail(userRegisterReq.getEmail());
        newUser.setPassword(passwordEncoder.encode(userRegisterReq.getPassword()));
        newUser.setActive(true);
    
        userService.createUser(newUser);
    
        return new ResponseMessage(true, "Đăng ký tài khoản thành công.");
    }
}
