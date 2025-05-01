package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.ResponseMessage;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User getUserByEmail(String email) {
    return this.userRepository.getUserByEmail(email);
  }

  public boolean isEmailExist(String email) {
    return this.userRepository.existsByEmail(email);
  }

  @Transactional
  public ResponseMessage createUser(User user) {
    user.setPassword(this.passwordEncoder.encode(user.getPassword()));
    boolean isEmailExist = this.isEmailExist(user.getEmail());
    if (isEmailExist) {
      return new ResponseMessage(false, "Email " + user.getEmail() + " đã tồn tại.");
    }
    this.userRepository.save(user);
    return new ResponseMessage(true, "Tạo người dùng thành công.");
  }
}
