package com.vn.fruitcart.controller;

import com.vn.fruitcart.entity.dto.request.UserRegisterReq;
import com.vn.fruitcart.service.AuthService;
import com.vn.fruitcart.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
  private final AuthService authService;
  private final UserService userService;

  public AuthController(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  @GetMapping("/login")
  public String getLoginPage() {
    return "admin/pages/auth/login";
  }

  @GetMapping("/register")
  public String getRegisterPage(Model model) {
    model.addAttribute("userReq", new UserRegisterReq());
    return "admin/pages/auth/register";
  }

  @PostMapping("/register")
  public String register(
      @Valid @ModelAttribute("userReq") UserRegisterReq userReq,
      BindingResult bindingResult,
      Model model) {

    if (!userReq.getPassword().equals(userReq.getRePassword())) {
      bindingResult.rejectValue("rePassword", "", "Mật khẩu xác nhận không khớp");
    }

    if (this.userService.isEmailExists(userReq.getEmail())) {
      bindingResult.rejectValue("email", "", "Email đã tồn tại");
    }

    if (bindingResult.hasErrors()) {
      return "admin/pages/auth/register";
    }

    this.authService.register(userReq);
    return "redirect:/login?success";
  }
}