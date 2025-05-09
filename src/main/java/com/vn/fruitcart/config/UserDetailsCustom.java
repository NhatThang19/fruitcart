package com.vn.fruitcart.config;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.UserService;
import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {
  private final UserService userService;

  public UserDetailsCustom(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = this.userService.getUserByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException("Tài khoản hoặc mật khẩu không hợp lệ");
    }
    
    if (!user.isActive()) {
      throw new DisabledException("User is disabled");
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
  }
}
