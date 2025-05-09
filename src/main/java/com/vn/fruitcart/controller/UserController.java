package com.vn.fruitcart.controller;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UpdateUserReq;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.ImgService;
import com.vn.fruitcart.service.RoleService;
import com.vn.fruitcart.service.SessionService;
import com.vn.fruitcart.service.UserService;
import com.vn.fruitcart.util.mapper.UserMapper;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin/users")
public class UserController {

  private final SessionService sessionService;
  private final UserService userService;
  private final ImgService imgService;
  private final RoleService roleService;

  public UserController(UserService userService, ImgService imgService, RoleService roleService,
      SessionService sessionService) {
    this.userService = userService;
    this.imgService = imgService;
    this.roleService = roleService;
    this.sessionService = sessionService;
  }

  @GetMapping("")
  public String getUsersPage(Model model) {
    List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
    segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
    segments.add(new PageMetadata.BreadcrumbSegment("Người dùng", "/admin/users"));

    PageMetadata pageMetadata = new PageMetadata("Người dùng", segments);
    model.addAttribute("pageMetadata", pageMetadata);
    return "admin/pages/users/view";
  }

  @GetMapping("/detail/{id}")
  public String getUserDetailPage(Model model, @PathVariable long id) {
    List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
    segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
    segments.add(new PageMetadata.BreadcrumbSegment("Người dùng", "/admin/users"));
    segments.add(new PageMetadata.BreadcrumbSegment("Chi tiết", "#"));

    PageMetadata pageMetadata = new PageMetadata("Chi tiết người dùng", segments);
    model.addAttribute("pageMetadata", pageMetadata);

    model.addAttribute("user", this.userService.getUserById(id));

    return "admin/pages/users/detail";
  }

  @GetMapping("/edit/{id}")
  public String getEditUserPage(Model model, @PathVariable long id) {

    User user = userService.getUserById(id);
    if (user == null) {
      return "redirect:/admin/users";
    }

    UpdateUserReq updateUserReq = UserMapper.convertToUpdateUserReq(user);

    model.addAttribute("user", updateUserReq);

    List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
    segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
    segments.add(new PageMetadata.BreadcrumbSegment("Người dùng", "/admin/users"));
    segments.add(new PageMetadata.BreadcrumbSegment("Chỉnh sửa", "#"));

    PageMetadata pageMetadata = new PageMetadata("Chỉnh sửa người dùng", segments);
    model.addAttribute("pageMetadata", pageMetadata);

    return "admin/pages/users/edit";
  }

  @PostMapping("/edit")
  public String updateUser(
      @Valid @ModelAttribute("user") UpdateUserReq updateUserReq,
      BindingResult bindingResult,
      @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
      RedirectAttributes redirectAttributes,
      Model model,
      Authentication authentication) { // Nhận thông tin người dùng hiện tại

    User existingUser = userService.getUserById(updateUserReq.getId());
    if (existingUser == null) {
      redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
      return "redirect:/admin/users";
    }

    if (bindingResult.hasErrors()) {
      setupEditPageModel(model);
      return "admin/pages/users/edit";
    }

    try {
      // Lưu role cũ để so sánh
      String oldRole = existingUser.getRole() != null ? existingUser.getRole().getName() : null;
      boolean roleChanged = false;

      // Xử lý upload avatar
      if (avatarFile != null && !avatarFile.isEmpty()) {
        String avatarFileName = imgService.storeFile(avatarFile, "avatars");
        updateUserReq.setAvatar("/storage/avatars/" + avatarFileName);
      } else {
        updateUserReq.setAvatar(existingUser.getAvatar());
      }

      // Cập nhật thông tin
      existingUser.setUsername(updateUserReq.getUsername());
      existingUser.setPhone(updateUserReq.getPhone());
      existingUser.setAddress(updateUserReq.getAddress());
      existingUser.setGender(updateUserReq.getGender());
      existingUser.setActive(updateUserReq.isActive());
      existingUser.setAvatar(updateUserReq.getAvatar());

      // Xử lý role
      if (updateUserReq.getRole() != null) {
        Role newRole = this.roleService.getRoleByName(updateUserReq.getRole());
        if (oldRole != null) {
            roleChanged = !oldRole.equals(updateUserReq.getRole());
        } else {
            roleChanged = true;
        }
        existingUser.setRole(newRole);
      }

      // Nếu role thay đổi, đăng xuất người dùng đó
      if (roleChanged) {
        sessionService.expireUserSessions(existingUser.getId());
      }

      userService.save(existingUser);
      
      redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
      return "redirect:/admin/users/detail/" + existingUser.getId();

    } catch (Exception e) {
      setupEditPageModel(model);
      model.addAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
      return "admin/pages/users/edit";
    }
  }

  private void setupEditPageModel(Model model) {
    List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
    segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
    segments.add(new PageMetadata.BreadcrumbSegment("Người dùng", "/admin/users"));
    segments.add(new PageMetadata.BreadcrumbSegment("Chỉnh sửa", "#"));

    PageMetadata pageMetadata = new PageMetadata("Chỉnh sửa người dùng", segments);
    model.addAttribute("pageMetadata", pageMetadata);
  }

}
