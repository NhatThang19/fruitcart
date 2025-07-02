package com.vn.fruitcart.controller.client;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.vn.fruitcart.config.UserDetailsCustom;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.profile.UserPasswordChangeReq;
import com.vn.fruitcart.entity.dto.request.profile.UserProfileUpdateReq;
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
    private final OrderService orderService;

    @GetMapping
    public String viewProfile(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildUserProfilePageMetadata());
        User currentUser = userDetailsCustom.getCurrentUserEntity(userService);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<Order> orderPage = orderService.findOrdersForCurrentUser(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdDate"))
        );

        model.addAttribute("userProfile", currentUser);
        model.addAttribute("orderPage", orderPage);

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
                .lastName(currentUser.getLastName()).gender(currentUser.getGender()).province(currentUser.getProvince())
                .district(currentUser.getDistrict()).ward(currentUser.getWard())
                .addressDetail(currentUser.getAddressDetail())
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
        model.addAttribute("pageMetadata", breadcrumbService.buildChangeUserPassPageMetadata());

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
            return "client/pages/profile/change-password";
        }

        try {
            userService.changePassword(passwordChangeReq, currentUser);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error_message", e.getMessage());
            return "client/pages/profile/change-password";
        }
    }

    @GetMapping("/orders/{id}")
    public String orderDetailPage(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return orderService.findOrderByIdForCurrentUser(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("pageMetadata", breadcrumbService.buildUserProfilePageMetadata());
                    return "client/pages/profile/order-detail";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn hàng này.");
                    return "redirect:/profile";
                });
    }

}
