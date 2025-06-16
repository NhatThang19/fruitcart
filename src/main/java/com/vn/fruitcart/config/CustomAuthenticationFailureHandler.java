package com.vn.fruitcart.config;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException, AuthenticationException {
        String redirectUrl = "/login?error"; 

        if (exception instanceof InternalAuthenticationServiceException) {
            redirectUrl = "/login?locked";
        } else if (exception instanceof BadCredentialsException) {
            redirectUrl = "/login?error";
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

}
