package com.vn.fruitcart.controller.client;

import com.vn.fruitcart.entity.dto.request.auth.UserRegisterReq;
import com.vn.fruitcart.service.AuthService;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final UserService userService;
  private final BreadcrumbService breadcrumbService;

  @GetMapping("/login")
  public String showLoginForm(Model model) {
    model.addAttribute("pageMetadata", breadcrumbService.buildLoginPageMetadata());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
        && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    return "client/pages/auth/login";
  }

  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    model.addAttribute("pageMetadata", breadcrumbService.buildRegisterPageMetadata());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
        && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    if (!model.containsAttribute("registerReq")) {
      model.addAttribute("registerReq", new UserRegisterReq());
    }

    return "client/pages/auth/register";
  }

  @PostMapping("/register")
  public String processRegistration(@Valid @ModelAttribute("registerReq") UserRegisterReq registerReq,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (!registerReq.getPassword().isEmpty() && !registerReq.getConfirmPassword().isEmpty() &&
        !registerReq.getPassword().equals(registerReq.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", "MatchPassword.registerReq.confirmPassword",
          "Mật khẩu xác nhận không khớp.");
    }

    if (registerReq.getEmail() != null && !registerReq.getEmail().isEmpty()
        && userService.existsByEmail(registerReq.getEmail())) {
      bindingResult.rejectValue("email", "Exist.registerReq.email", "Địa chỉ email này đã được sử dụng.");
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("pageMetadata", breadcrumbService.buildRegisterPageMetadata());
      return "client/pages/auth/register";
    }

    try {
      authService.register(registerReq);
      redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
      return "redirect:/login?register";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      redirectAttributes.addFlashAttribute("registerReq", registerReq);
      return "redirect:/admin/register";
    }
  }

}