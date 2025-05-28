package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserProfileUpdateReq;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final FileStorageService fileStorageService;
  private final HttpSession session;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      FileStorageService fileStorageService, HttpSession session) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.fileStorageService = fileStorageService;
    this.session = session;
  }

  public User findUserByEmail(String email) {
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

  @Transactional
  public User updateUserProfile(Long userId, UserProfileUpdateReq updateReq) {
    User userToUpdate = this.findUserById(userId);

    userToUpdate.setFirstName(updateReq.getFirstName());
    userToUpdate.setLastName(updateReq.getLastName());
    userToUpdate.setPhone(updateReq.getPhone());
    userToUpdate.setAddress(updateReq.getAddress());
    userToUpdate.setGender(updateReq.getGender());
    userToUpdate.setBirthDate(updateReq.getBirthDate());

    MultipartFile avatarFile = updateReq.getAvatarFile();
    if (avatarFile != null && !avatarFile.isEmpty()) {
      // if (userToUpdate.getAvatarUrl() != null &&
      // !userToUpdate.getAvatarUrl().isEmpty()) {
      // fileStorageService.deleteFile(userToUpdate.getAvatarUrl());
      // }
      String newAvatarFileName = fileStorageService.storeFile(avatarFile, "avatars");
      userToUpdate.setAvatarUrl(newAvatarFileName);
    } else if (updateReq.getCurrentAvatar() != null) {
      userToUpdate.setAvatarUrl(updateReq.getCurrentAvatar());
    }

    User updatedUserEntity = userRepository.save(userToUpdate);

    this.updateLoggedSession(updatedUserEntity);

    return updatedUserEntity;
  }

  public void updateLoggedSession(User user) {
    UserSessionInfo userSessionInfo = new UserSessionInfo();
    userSessionInfo.setUserId(user.getId());
    userSessionInfo.setEmail(user.getEmail());
    userSessionInfo.setFullName(user.getFullName());
    userSessionInfo.setAvatar(user.getAvatarUrl());
    userSessionInfo.setRole(user.getRole().getName());

    session.setAttribute("loggedInUser", userSessionInfo);
  }

  public User findUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với Id: " + id));
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
