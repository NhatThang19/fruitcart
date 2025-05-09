package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User getUserByEmail(String email) {
    return this.userRepository.getUserByEmail(email);
  }

  public boolean isEmailExists(String email) {
    return this.userRepository.existsByEmail(email);
  }

  @Transactional
  public User createUser(User user) {
    user.setPassword(this.passwordEncoder.encode(user.getPassword()));
    this.userRepository.save(user);
    return this.userRepository.save(user);
  }

  public User getUserById(Long id) {
    return this.userRepository.findById(id).orElse(null);
  }

  public User save(User existingUser) {
    return this.userRepository.save(existingUser);
  }
}
