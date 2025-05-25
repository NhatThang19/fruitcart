package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
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

  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  public User save(User existingUser) {
    return this.userRepository.save(existingUser);
  }

  public void deleteUserById(Long id) {
    this.userRepository.deleteById(id);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhone(String phone) {
    return userRepository.existsByPhone(phone);
  }
}
