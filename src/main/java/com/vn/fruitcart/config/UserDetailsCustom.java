package com.vn.fruitcart.config;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
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

    if (user.getIsBlocked() != null && user.getIsBlocked()) {
      throw new LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName())));
  }

}