package com.vn.fruitcart.config;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserBlockInterceptor implements HandlerInterceptor {

    private final SessionRegistry sessionRegistry;
    private final UserService userService;

    public UserBlockInterceptor(SessionRegistry sessionRegistry, UserService userService) {
        this.sessionRegistry = sessionRegistry;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            User user = this.userService.getUserByEmail(email);
            
            if (user == null) {
                SecurityContextHolder.clearContext();
                response.sendRedirect("/login?error");
                return false;
            }

            if (!user.isActive()) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(auth.getPrincipal(), false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }

                SecurityContextHolder.clearContext();
                response.sendRedirect("/login?blocked");
                return false;
            }
        }

        return true;
    }
}