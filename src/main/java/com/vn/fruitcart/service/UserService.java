package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserClusteringData;
import com.vn.fruitcart.entity.dto.request.profile.UserPasswordChangeReq;
import com.vn.fruitcart.entity.dto.request.profile.UserProfileUpdateReq;
import com.vn.fruitcart.entity.dto.request.user.AdminUserUpdateReq;
import com.vn.fruitcart.entity.dto.request.user.UserSearchCriteriaReq;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.entity.dto.response.user.AdminUserDetailRes;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.repository.UserRepository;
import com.vn.fruitcart.service.specification.UserSpecification;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;
  private final FileStorageService fileStorageService;
  private final HttpSession session;
  private final SessionRegistry sessionRegistry;
  private final OrderRepository orderRepository;

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
  }

  public void checkEmailDoesNotExist(String email) {
    userRepository.findByEmail(email).ifPresent(user -> {
      throw new IllegalArgumentException("Email '" + email + "' đã được sử dụng.");
    });
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
  public User updateUserProfile(Long userId, UserProfileUpdateReq req) {
    User userToUpdate = this.getUserById(userId);

    userToUpdate.setFirstName(req.getFirstName());
    userToUpdate.setLastName(req.getLastName());
    userToUpdate.setPhone(req.getPhone());
    userToUpdate.setAddress(req.getAddress());
    userToUpdate.setGender(req.getGender());
    userToUpdate.setBirthDate(req.getBirthDate());

    MultipartFile avatarFile = req.getAvatarFile();
    if (avatarFile != null && !avatarFile.isEmpty()) {
      if (userToUpdate.getAvatarUrl() != null &&
          !userToUpdate.getAvatarUrl().isEmpty()) {
        fileStorageService.deleteFile(userToUpdate.getAvatarUrl());
      }
      String newAvatarFileName = fileStorageService.storeFile(avatarFile, "avatars");
      userToUpdate.setAvatarUrl(newAvatarFileName);
    } else if (req.getCurrentAvatar() != null) {
      userToUpdate.setAvatarUrl(req.getCurrentAvatar());
    }

    User updatedUserEntity = userRepository.save(userToUpdate);

    this.updateLoggedSession(updatedUserEntity);

    return updatedUserEntity;
  }

  @Transactional
  public void changePassword(UserPasswordChangeReq request, User currentUser) {
    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
      throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
    }

    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
    }

    if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
      throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
    }

    currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(currentUser);
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

  public Page<User> findAllUsers(Specification<User> spec, Pageable pageable) {
    return userRepository.findAll(spec, pageable);
  }

  public Page<User> findUsersByCriteria(UserSearchCriteriaReq criteria, Pageable pageable) {
    Specification<User> spec = UserSpecification.filterBy(criteria);

    return userRepository.findAll(spec, pageable);
  }

  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với Id: " + id));
  }

  @Transactional
  public User updateUserRoleAndStatusByAdmin(AdminUserUpdateReq req) throws RuntimeException {
    User user = userRepository.findById(req.getUserId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + req.getUserId()));

    Role oldRole = user.getRole();
    Role newRole = roleService.findRoleById(req.getRoleId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + req.getRoleId()));

    user.setRole(newRole);
    user.setIsBlocked(req.getIsBlocked());
    User updatedUser = userRepository.save(user);

    boolean roleActuallyChanged = !oldRole.getId().equals(newRole.getId());

    boolean userIsNowBlockedByRequest = req.getIsBlocked();

    if (roleActuallyChanged || userIsNowBlockedByRequest) {
      expireUserSessions(updatedUser.getId());
    }
    return updatedUser;
  }

  public User save(User existingUser) {
    return this.userRepository.save(existingUser);
  }

  @Transactional
  public void deleteUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    userRepository.delete(user);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhone(String phone) {
    return userRepository.existsByPhone(phone);
  }

  @Transactional
  public void expireUserSessions(Long userId) {
    String userEmail = this.getUserById(userId).getEmail();

    sessionRegistry.getAllPrincipals().forEach(principal -> {
      if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        if (userDetails.getUsername().equals(userEmail)) {
          sessionRegistry.getAllSessions(principal, false).forEach(session -> {
            session.expireNow();
          });
        }
      }
    });
  }

  public AdminUserDetailRes getAdminUserDetail(Long userId) {
    // 1. Lấy thông tin người dùng
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

    // 2. Lấy lịch sử đơn hàng
    List<Order> orderHistory = user.getOrders();

    orderHistory.sort(Comparator.comparing(Order::getCreatedDate).reversed());

    // 3. Tính toán các chỉ số mua hàng
    long totalOrders = orderHistory.size();

    BigDecimal totalSpending = orderRepository.findTotalSpendingByUser(user).orElse(BigDecimal.ZERO);

    BigDecimal averageOrderValue = (totalOrders > 0)
        ? totalSpending.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    Instant ninetyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);
    long purchaseFrequencyLast90Days = orderRepository.countByUserAndCreatedDateAfter(user, ninetyDaysAgo);

    Long daysSinceLastPurchase = orderRepository.findMostRecentOrderDateByUser(user)
        .map(lastPurchaseDate -> Duration.between(lastPurchaseDate, Instant.now()).toDays())
        .orElse(null);

    // 4. Tạo và trả về DTO
    return new AdminUserDetailRes(
        user,
        orderHistory,
        totalOrders,
        totalSpending,
        averageOrderValue,
        purchaseFrequencyLast90Days,
        daysSinceLastPurchase);
  }

  public List<UserClusteringData> getAllUsersClusteringData() {
    List<User> users = userRepository.findAll(); // Lấy tất cả user
    List<UserClusteringData> clusteringDataList = new ArrayList<>();

    for (User user : users) {
      long totalOrders = user.getOrders().size();

      // Nếu user không có đơn hàng nào, bỏ qua
      if (totalOrders == 0) {
        continue;
      }

      BigDecimal totalSpending = orderRepository.findTotalSpendingByUser(user).orElse(BigDecimal.ZERO);
      BigDecimal averageOrderValue = totalSpending.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
      Instant ninetyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);
      long purchaseFrequencyLast90Days = orderRepository.countByUserAndCreatedDateAfter(user, ninetyDaysAgo);
      Long daysSinceLastPurchase = orderRepository.findMostRecentOrderDateByUser(user)
          .map(lastDate -> Duration.between(lastDate, Instant.now()).toDays())
          .orElse(null);

      clusteringDataList.add(new UserClusteringData(
          user,
          totalSpending,
          totalOrders,
          averageOrderValue,
          purchaseFrequencyLast90Days,
          daysSinceLastPurchase,
          -1 // Cluster ban đầu là -1 (chưa gán)
      ));
    }
    return clusteringDataList;
  }

}
