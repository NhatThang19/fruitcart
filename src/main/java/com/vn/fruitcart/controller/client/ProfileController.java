package com.vn.fruitcart.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.vn.fruitcart.config.UserDetailsCustom;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserProfileUpdateReq;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserDetailsCustom userDetailsCustom;
    private final UserService userService;
    private final BreadcrumbService breadcrumbService;

    public ProfileController(UserService userService, BreadcrumbService breadcrumbService,
            UserDetailsCustom userDetailsCustom) {
        this.userService = userService;
        this.breadcrumbService = breadcrumbService;
        this.userDetailsCustom = userDetailsCustom;
    }

    @GetMapping
    public String viewProfile(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildUserProfilePageMetadata());
        User currentUser = userDetailsCustom.getCurrentUserEntity(userService);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("userProfile", currentUser);

        return "client/pages/profile/detail";
    }

    @GetMapping("/update")
    public String getUpdateProfileForm(Model model, HttpSession session) {
        model.addAttribute("pageMetadata", breadcrumbService.buildUpdateUserProfilePageMetadata());

        User currentUser = userDetailsCustom.getCurrentUserEntity(userService);
        if (currentUser == null) {
            return "redirect:/login";
        }

        UserProfileUpdateReq userProfileUpdateReq = UserProfileUpdateReq.builder().firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName()).gender(currentUser.getGender()).address(currentUser.getAddress())
                .birthDate(currentUser.getBirthDate()).currentAvatar(currentUser.getAvatarUrl())
                .phone(currentUser.getPhone()).email(currentUser.getEmail()).build();

        model.addAttribute("userProfileUpdateReq", userProfileUpdateReq);
        return "client/pages/profile/update";
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("userProfileUpdateReq") UserProfileUpdateReq userProfileUpdateReq,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = userDetailsCustom.getCurrentUserEntity(userService);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageMetadata", breadcrumbService.buildUpdateUserProfilePageMetadata());
            return "client/pages/profile/update";
        }

        try {
            userService.updateUserProfile(currentUser.getId(), userProfileUpdateReq);
            redirectAttributes.addFlashAttribute("message", "Sửa trang cái nhân thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("breadcrumbs", breadcrumbService.buildUpdateUserProfilePageMetadata());
            model.addAttribute("userProfileUpdateReq", userProfileUpdateReq);
            redirectAttributes.addFlashAttribute("message", "Xoá người dùng thất bại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "client/pages/profile/update";
        }

        return "redirect:/profile";
    }

}
