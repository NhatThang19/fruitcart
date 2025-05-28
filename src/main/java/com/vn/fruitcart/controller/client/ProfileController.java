package com.vn.fruitcart.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.vn.fruitcart.config.UserDetailsCustom;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UserPasswordChangeReq;
import com.vn.fruitcart.entity.dto.request.UserProfileUpdateReq;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserDetailsCustom userDetailsCustom;
    private final UserService userService;
    private final BreadcrumbService breadcrumbService;

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
    public String getUpdateProfileForm(Model model) {
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
            redirectAttributes.addFlashAttribute("message", "Sửa trang cá nhân thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("pageMetadata", breadcrumbService.buildUpdateUserProfilePageMetadata());
            model.addAttribute("userProfileUpdateReq", userProfileUpdateReq);
            redirectAttributes.addFlashAttribute("message", "Sửa trang cá nhân người dùng thất bại.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "client/pages/profile/update";
        }

        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String getChangePasswordPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildChangeUserPassPageMetadata());
        model.addAttribute("passwordChangeReq", new UserPasswordChangeReq());

        return "client/pages/profile/change-password";
    }

    @PostMapping("/change-password")
    public String handleChangePassword(
            @Valid @ModelAttribute("passwordChangeReq") UserPasswordChangeReq passwordChangeReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        User currentUser = userDetailsCustom.getCurrentUserEntity(userService);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!passwordChangeReq.getConfirmNewPassword().isEmpty() && !passwordChangeReq.getConfirmNewPassword().isEmpty()
                &&
                !passwordChangeReq.getNewPassword().equals(passwordChangeReq.getConfirmNewPassword())) {
            bindingResult.rejectValue("confirmNewPassword", "",
                    "Mật khẩu xác nhận không khớp.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageMetadata", breadcrumbService.buildChangeUserPassPageMetadata());
            return "client/pages/profile/change-password";
        }

        try {
            userService.changePassword(passwordChangeReq, currentUser);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error_message", e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thất bại.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "client/pages/profile/change-password";
        }
        return "redirect:/profile";
    }

}
