package com.vn.fruitcart.controller;

import com.vn.fruitcart.entity.dto.request.UserRegisterReq;
import com.vn.fruitcart.entity.dto.response.ResponseMessage;
import com.vn.fruitcart.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
  private final AuthService authService;

  private AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  public String getLoginPage(Model model) {
    return "admin/pages/auth/login";
  }

  @GetMapping("/register")
  public String getRegisterPage(Model model) {
    model.addAttribute("userReq", new UserRegisterReq());
    return "admin/pages/auth/register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute @Valid UserRegisterReq userReq,
      BindingResult bindingResult,
      RedirectAttributes redirectAttribute,
      Model model) {

    if (bindingResult.hasErrors()) {
      return "/register";
    }

    ResponseMessage response = this.authService.register(userReq);
    if (!response.isSuccess()) {
      model.addAttribute("error", response.getMessage());
      return "register";
    }

    model.addAttribute("success", response.getMessage());

    return "redirect:/login";
  }

}
