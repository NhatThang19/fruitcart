package com.vn.fruitcart.controller.admin;

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

import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.request.OriginReq;
import com.vn.fruitcart.entity.dto.request.category.OriginSearchCriteria;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OriginService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.util.FruitCartUtils;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/origins")
@RequiredArgsConstructor
public class AdminOriginController {
    private final OriginService originService;
    private final ProductService productService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listOrigins(Model model,
            @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable,
            @ModelAttribute("criteria") OriginSearchCriteria criteria) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginPageMetadata());

        Page<Origin> originPage = originService.findOriginsByCriteria(criteria, pageable);

        model.addAttribute("originPage", originPage);

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

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

    @PostMapping("/delete/{originId}")
    public String deleteOrigin(@PathVariable("originId") Long id, RedirectAttributes redirectAttributes) {
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
            @PageableDefault(size = 5, sort = "name") Pageable pageable,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        try {
            Origin origin = originService.getOriginById(id);
            model.addAttribute("origin", origin);
            Page<Product> productPage = productService.findPaginatedByOriginId(id, pageable);
            model.addAttribute("productPage", productPage);
            return "admin/pages/origin/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn gốc: " + e.getMessage());
            return "redirect:/admin/origins";
        }
    }

}
