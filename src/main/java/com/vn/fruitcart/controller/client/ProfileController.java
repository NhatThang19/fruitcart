package com.vn.fruitcart.controller.client;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserProfileUpdateReq;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final BreadcrumbService breadcrumbService;

    public ProfileController(UserService userService, BreadcrumbService breadcrumbService) {
        this.userService = userService;
        this.breadcrumbService = breadcrumbService;
    }

    @GetMapping
    public String viewProfile(Model model, HttpSession session) {
        model.addAttribute("pageMetadata", breadcrumbService.buildUserProfilePageMetadata());
        UserSessionInfo userSessionInfo = (UserSessionInfo) session.getAttribute("loggedInUser");
        if (userSessionInfo == null) {
            return "redirect:/login";
        }
        User currentUser = userService.getUserById(userSessionInfo.getUserId()).orElse(null);
        model.addAttribute("userProfile", currentUser);

        return "client/pages/profile/detail";
    }

    @GetMapping("/edit")
    public String getUpdateProfileForm(Model model, HttpSession session) {
        model.addAttribute("pageMetadata", breadcrumbService.buildUpdateUserProfilePageMetadata());

        UserSessionInfo userSessionInfo = (UserSessionInfo) session.getAttribute("loggedInUser");
        if (userSessionInfo == null) {
            return "redirect:/login";
        }

        User currentUser = userService.getUserById(userSessionInfo.getUserId()).orElse(null);
        UserProfileUpdateReq updateReq = new UserProfileUpdateReq();
        BeanUtils.copyProperties(currentUser, updateReq);
        return "client/pages/profile/update";
    }

}
