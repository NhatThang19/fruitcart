package com.vn.fruitcart.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.AdminUserUpdateReq;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.RoleService;
import com.vn.fruitcart.service.UserService;

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
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Model model,
            @RequestParam(name = "emailSearchValue", required = false) String emailSearch,
            @RequestParam(name = "roleIdSearchValue", required = false) Integer roleId,
            @RequestParam(name = "isBlockedSearchValue", required = false) Boolean isBlockedStatus) {

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserLisPageMetadata());

        Page<User> usersPage = userService.findUsersByCriteria(emailSearch, roleId, isBlockedStatus, pageable);
        List<Role> allRoles = roleService.findAll();

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("allRoles", allRoles);

        model.addAttribute("currentEmailSearch", emailSearch);
        model.addAttribute("currentRoleId", roleId);
        model.addAttribute("currentIsBlockedStatus", isBlockedStatus);
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("allRoles", allRoles);

        model.addAttribute("currentEmailSearch", emailSearch);
        model.addAttribute("currentRoleId", roleId);
        model.addAttribute("currentIsBlockedStatus", isBlockedStatus);

        String currentSortField = "";
        String currentSortDir = "asc";
        if (pageable.getSort().isSorted()) {
            currentSortField = pageable.getSort().get().findFirst().get().getProperty();
            currentSortDir = pageable.getSort().get().findFirst().get().getDirection().name().toLowerCase();
        }
        model.addAttribute("currentSortField", currentSortField);
        model.addAttribute("currentSortDir", currentSortDir);
        model.addAttribute("reverseSortDir", currentSortDir.equals("asc") ? "desc" : "asc");

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

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserUpdatePageMetadata());

        Optional<User> userOptional = userService.findUserByIdOptional(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }
        User user = userOptional.get();

        AdminUserUpdateReq adminUserUpdateReq = new AdminUserUpdateReq();
        adminUserUpdateReq.setUserId(user.getId());
        if (user.getRole() != null) {
            adminUserUpdateReq.setRoleId(user.getRole().getId());
        }
        adminUserUpdateReq.setIsBlocked(user.getIsBlocked());

        model.addAttribute("adminUserUpdateReq", adminUserUpdateReq);
        model.addAttribute("userToManage", user);
        List<Role> allRoles = roleService.findAll();
        model.addAttribute("allRoles", allRoles);

        return "admin/pages/user/update";
    }

    @PostMapping("/update-admin-settings")
    public String updateUserAdminSettings(
            @Valid @ModelAttribute("adminUserUpdateReq") AdminUserUpdateReq adminUserUpdateReq,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminUserUpdatePageMetadata());
            User userToManage = userService.findUserByIdOptional(adminUserUpdateReq.getUserId()).orElse(null);
            model.addAttribute("userToManage", userToManage);
            List<Role> allRoles = roleService.findAll();
            model.addAttribute("allRoles", allRoles);
            return "admin/pages/user/update";
        }
        try {
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
                System.out.println("Xoa thanh cong!!!");
                redirectAttributes.addFlashAttribute("message", "Xoá người dùng thành công.");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Xoá người dùng thất bại.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }

}
