package com.vn.fruitcart.controller;

import com.vn.fruitcart.entity.dto.request.RegisterReq;
import com.vn.fruitcart.service.AuthService;
import com.vn.fruitcart.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
  private final AuthService authService;
  private final UserService userService;

  public AuthController(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

@GetMapping("/login")
public String showLoginForm(
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String expired,
        @RequestParam(required = false) String banned,
        @RequestParam(required = false) String logout,
        Model model) {

    if (error != null) {
        model.addAttribute("loginErrorMessage", "Tên đăng nhập hoặc mật khẩu không đúng.");
    }

    if (expired != null) {
        model.addAttribute("loginErrorMessage", "Phiên làm việc đã hết hạn.");
    }

    if (banned != null) {
        model.addAttribute("loginErrorMessage", "Tài khoản của bạn đã bị khóa.");
    }

    if (logout != null) {
        model.addAttribute("logoutMessage", "Đăng xuất thành công.");
    }

    return "admin/pages/auth/login";
}

  @GetMapping("/register")
  public String getRegisterPage(Model model) {
    model.addAttribute("userReq", new RegisterReq());
    return "admin/pages/auth/register";
  }

  @PostMapping("/register")
  public String register(
      @Valid @ModelAttribute("userReq") RegisterReq userReq,
      BindingResult bindingResult,
      Model model) {

    if (!userReq.getPassword().equals(userReq.getRePassword())) {
      bindingResult.rejectValue("rePassword", "", "Mật khẩu xác nhận không khớp.");
    }

    if (this.userService.isEmailExists(userReq.getEmail())) {
      bindingResult.rejectValue("email", "", "Email đã tồn tại.");
    }

    if (bindingResult.hasErrors()) {
      return "admin/pages/auth/register";
    }

    this.authService.register(userReq);
    return "redirect:/login?success";
  }
}