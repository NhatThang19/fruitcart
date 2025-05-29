package com.vn.fruitcart.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.RoleService;
import com.vn.fruitcart.service.UserService;

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
}
