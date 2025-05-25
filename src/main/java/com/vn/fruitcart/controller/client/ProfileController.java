package com.vn.fruitcart.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.service.UserService;

@Controller
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequestMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        UserSessionInfo userSessionInfo = (UserSessionInfo) session.getAttribute("loggedInUser");
        if (userSessionInfo == null) {
            return "redirect:/login";
        }
        User currentUser = userService.getUserById(userSessionInfo.getUserId()).orElse(null);
        model.addAttribute("userProfile", currentUser);

        return "client/pages/profile";
    }
}
