package com.vn.fruitcart.controller.admin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
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

import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.dto.request.OriginReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OriginService;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/origins")
@RequiredArgsConstructor
public class AdminOriginController {
    private final OriginService originService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listOrigins(Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginPageMetadata());

        String sortField = sort[0];
        String sortDir = (sort.length > 1 && (sort[1].equalsIgnoreCase("asc") || sort[1].equalsIgnoreCase("desc")))
                ? sort[1]
                : "asc";
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        Boolean statusFilter = null;
        if (status != null && !status.isEmpty()) {
            if ("true".equalsIgnoreCase(status)) {
                statusFilter = true;
            } else if ("false".equalsIgnoreCase(status)) {
                statusFilter = false;
            }
        }
        model.addAttribute("selectedStatus", status == null ? "" : status);

        Page<Origin> originPage = originService.getAllOriginsWithFiltersAndPagination(keyword, statusFilter,
                page, size, sortField, sortDir);

        model.addAttribute("origins", originPage.getContent());
        model.addAttribute("currentPage", originPage.getNumber() + 1);
        model.addAttribute("totalPages", originPage.getTotalPages());
        model.addAttribute("totalItems", originPage.getTotalElements());
        model.addAttribute("pageSize", size);

        if (originPage.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, originPage.getTotalPages())
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("keyword", keyword);

        return "admin/pages/origin/list";
    }

    @GetMapping("/create")
    public String showAddOriginForm(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginCreatePageMetadata());
        model.addAttribute("originReq", new OriginReq());

        return "admin/pages/origin/create";
    }

    @PostMapping("/create")
    public String createOrigin(@Valid @ModelAttribute("originReq") OriginReq originReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginCreatePageMetadata());
        if (bindingResult.hasErrors()) {
            return "admin/pages/origin/create";
        }
        try {
            originService.createOrigin(originReq);
            redirectAttributes.addFlashAttribute("message", "Thêm nguồn gốc thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm nguồn gốc '" + originReq.getName() + "'' thành công");
            return "redirect:/admin/origins";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm nguồn gốc: " + e.getMessage());
            return "redirect:/admin/origins/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm nguồn gốc: " + e.getMessage());
            return "redirect:/admin/origins/create";
        }
    }

    @GetMapping("/update/{id}")
    public String showEditOriginForm(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginUpdatePageMetadata());

            Origin origin = originService.getOriginById(id);
            OriginReq originReq = new OriginReq();
            originReq.setName(origin.getName());
            originReq.setDescription(origin.getDescription());
            originReq.setStatus(origin.getStatus());

            model.addAttribute("originReq", originReq);
            model.addAttribute("originId", id);

            return "admin/pages/origin/update";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/origins";
        }
    }

    @PostMapping("/update/{id}")
    public String processUpdateOrigin(@PathVariable("id") Long id,
            @Valid @ModelAttribute("originReq") OriginReq originReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginUpdatePageMetadata());
        if (bindingResult.hasErrors()) {
            model.addAttribute("originId", id);
            return "admin/pages/origin/update";
        }

        try {
            originService.updateOrigin(id, originReq);
            redirectAttributes.addFlashAttribute("message", "Cập nhật nguồn gốc thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật nguồn gốc '" + originReq.getName() + "' thành công.");
            return "redirect:/admin/origins";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/origins";
        } catch (DuplicateNameException e) {
            bindingResult.rejectValue("name", "error.originReq", e.getMessage());
            model.addAttribute("originId", id);
            return "admin/pages/origin/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật nguồn gốc: " + e.getMessage());
            return "redirect:/admin/origins/update/" + id;
        }
    }

    @PostMapping("/delete")
    public String deleteOrigin(@RequestParam("originId") Long id, RedirectAttributes redirectAttributes) {
        try {
            originService.deleteOrigin(id);
            redirectAttributes.addFlashAttribute("message", "Xoá nguồn gốc thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nguồn gốc thành công.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa nguồn gốc.");
        }
        return "redirect:/admin/origins";
    }

    @GetMapping("/detail/{id}")
    public String showOriginDetail(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
        try {
            Origin origin = originService.getOriginById(id);
            model.addAttribute("origin", origin);
            return "admin/pages/origin/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn gốc: " + e.getMessage());
            return "redirect:/admin/origins";
        }
    }

}
