package com.vn.fruitcart.config;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsCustom implements UserDetailsService {

  @Autowired
  private UserService userService;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, LockedException {
    User user = userService.getUserByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException("Email không tồn tại trong hệ thống");
    }

    if (user.getIsBlocked() != null && user.getIsBlocked()) {
      throw new LockedException("Tài khoản bị khoá.");
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
  }

  public UserDetails getCurrentUserDetails() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof UserDetails) {
      return (UserDetails) authentication.getPrincipal();
    }
    return null;
  }

  public String getCurrentUsername() {
    UserDetails userDetails = getCurrentUserDetails();
    return userDetails != null ? userDetails.getUsername() : null;
  }

  public User getCurrentUserEntity(UserService userService) {
    String username = getCurrentUsername();
    if (username != null) {
      return userService.getUserByEmail(username);
    }
    return null;
  }
}