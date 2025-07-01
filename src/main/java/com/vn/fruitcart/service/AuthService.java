package com.vn.fruitcart.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.auth.UserRegisterReq;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    @Transactional
    public void register(UserRegisterReq req) {

        userService.checkEmailDoesNotExist(req.getEmail());

        Role clientRole = roleService.getRoleByName("User");

        User newUser = User.builder()
                .lastName(req.getLastName())
                .firstName(req.getFirstName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .province(req.getProvince()).district(req.getDistrict()).ward(req.getWard())
                .addressDetail(req.getAddressDetail())
                .password(passwordEncoder.encode(req.getPassword()))
                .isBlocked(false)
                .avatarUrl("/storage/default/avatar.jpg")
                .role(clientRole)
                .build();

        userService.save(newUser);
    }
}
