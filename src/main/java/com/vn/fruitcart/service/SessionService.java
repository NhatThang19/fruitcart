package com.vn.fruitcart.service;

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
        String userEmail = userService.getUserById(userId).getEmail();

        sessionRegistry.getAllPrincipals().forEach(principal -> {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                if (userDetails.getUsername().equals(userEmail)) {
                    sessionRegistry.getAllSessions(principal, false).forEach(session -> {
                        session.expireNow();
                    });
                }
            }
        });
    }
}