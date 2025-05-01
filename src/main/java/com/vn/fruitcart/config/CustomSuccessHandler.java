package com.vn.fruitcart.config;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class CustomSuccessHandler implements AuthenticationSuccessHandler {

  private final UserService userService;

  public CustomSuccessHandler(UserService userService) {
    this.userService = userService;
  }

  private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    String targetUrl = determineTargetUrl(authentication);
    clearAuthenticationAttributes(request, authentication);
    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_USER"))
        .findFirst()
        .map(role -> role.equals("ROLE_ADMIN") ? "/admin" : "/")
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy vai trò hợp lệ"));
  }

  protected void clearAuthenticationAttributes(HttpServletRequest request, Authentication authentication) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return;
    }

    String email = authentication.getName();
    User user = this.userService.getUserByEmail(email);

    if (user == null) {
      return;
    }

    UserSessionInfo info = new UserSessionInfo();
    info.setId(user.getId());
    info.setUsername(user.getUsername());
    info.setEmail(user.getEmail());
    info.setAvatar(user.getAvatar());

    session.setAttribute("userInfo", info);
  }
}
