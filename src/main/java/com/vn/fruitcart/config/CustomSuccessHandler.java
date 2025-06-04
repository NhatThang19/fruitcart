package com.vn.fruitcart.config;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  @Autowired
  @Lazy
  private UserService userService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String targetUrl = determineTargetUrl(authentication, request.getSession());

    if (response.isCommitted()) {
      return;
    }

    String email = authentication.getName();
    try {
      User user = userService.getUserByEmail(email);

      UserSessionInfo userSessionInfo = new UserSessionInfo();
      userSessionInfo.setUserId(user.getId());
      userSessionInfo.setEmail(user.getEmail());
      userSessionInfo.setFullName(user.getFullName());
      userSessionInfo.setAvatar(user.getAvatarUrl());
      if (user.getRole() != null) {
        userSessionInfo.setRole(user.getRole().getName());
        request.getSession().setAttribute("loggedInUser", userSessionInfo);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(Authentication authentication, HttpSession session) {
    String targetUrl;

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    boolean isAdmin = false;

    for (GrantedAuthority grantedAuthority : authorities) {
      if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
        isAdmin = true;
        break;
      }
    }

    if (isAdmin) {
      targetUrl = "/admin";
    } else {
      String previousPage = (String) session.getAttribute("previousPage");
      if (previousPage != null && !previousPage.isEmpty() && !previousPage.contains("/login")
          && !previousPage.contains("/register")) {
        targetUrl = previousPage;
        session.removeAttribute("previousPage");
      } else {
        targetUrl = "/";
      }
    }
    return targetUrl;
  }
}