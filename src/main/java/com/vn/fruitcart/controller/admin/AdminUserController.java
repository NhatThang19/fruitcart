package com.vn.fruitcart.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.user.AdminUserUpdateReq;
import com.vn.fruitcart.entity.dto.request.user.UserSearchCriteriaReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.RoleService;
import com.vn.fruitcart.service.UserService;
import com.vn.fruitcart.util.FruitCartUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    private final RoleService roleService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String getUsersPage(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Direction.DESC) Pageable pageable,
            @ModelAttribute("criteria") UserSearchCriteriaReq criteria) {

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserLisPageMetadata());

        List<Role> allRoles = roleService.findAll();

        Page<User> usersPage = userService.findUsersByCriteria(criteria, pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("allRoles", allRoles);

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/user/list";
    }

    @GetMapping("/{id}")
    public String getUserDetailPage(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserDetailPageMetadata());
            return "admin/pages/user/detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }

    }

    @GetMapping("/update/{id}")
    public String getAdminUpdateUserPage(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserUpdatePageMetadata());

            User user = userService.getUserById(id);
            AdminUserUpdateReq adminUserUpdateReq = new AdminUserUpdateReq();
            adminUserUpdateReq.setUserId(user.getId());
            if (user.getRole() != null) {
                adminUserUpdateReq.setRoleId(user.getRole().getId());
            }
            adminUserUpdateReq.setIsBlocked(user.getIsBlocked());

            model.addAttribute("adminUserUpdateReq", adminUserUpdateReq);
            model.addAttribute("userToManage", user);
            model.addAttribute("allRoles", roleService.findAll());

            return "admin/pages/user/update";

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/update")
    public String updateUserAdminSettings(
            @Valid @ModelAttribute("adminUserUpdateReq") AdminUserUpdateReq adminUserUpdateReq,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserUpdatePageMetadata());
        if (bindingResult.hasErrors()) {
            User userToManage = userService.getUserById(adminUserUpdateReq.getUserId());
            model.addAttribute("userToManage", userToManage);
            List<Role> allRoles = roleService.findAll();
            model.addAttribute("allRoles", allRoles);
            return "admin/pages/user/update";
        }
        try {
            userService.expireUserSessions(adminUserUpdateReq.getUserId());
            userService.updateUserRoleAndStatusByAdmin(adminUserUpdateReq);
            redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/admin/users/" + adminUserUpdateReq.getUserId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thất bại.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/users/manage/" + adminUserUpdateReq.getUserId();
        }
    }

    @PostMapping("/delete/{userId}")
    public String deleteUser(@PathVariable("userId") Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                userService.expireUserSessions(id);
                userService.deleteUserById(id);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Người dùng " + user.getFullName() + " đã được xóa thành công!");
                redirectAttributes.addFlashAttribute("message", "Xoá người dùng thành công.");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa người dùng thất bại: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", "Xoá người dùng thất bại.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }

}
