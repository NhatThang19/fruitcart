package com.vn.fruitcart.service;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class SessionService {
    private final SessionRegistry sessionRegistry;
    private final UserService userService;

    public SessionService(SessionRegistry sessionRegistry, UserService userService) {
        this.sessionRegistry = sessionRegistry;
        this.userService = userService;
    }

    public void expireUserSessions(Long userId) {
        // Lấy thông tin user cần expire
        String userEmail = userService.getUserById(userId).getEmail();
        
        // Lấy tất cả các principals đang active
        sessionRegistry.getAllPrincipals().forEach(principal -> {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                // Kiểm tra nếu principal là user cần expire bằng email
                if (userDetails.getUsername().equals(userEmail)) {
                    // Lấy tất cả session của user đó và expire
                    sessionRegistry.getAllSessions(principal, false).forEach(session -> {
                        session.expireNow();
                    });
                }
            }
        });
    }
}