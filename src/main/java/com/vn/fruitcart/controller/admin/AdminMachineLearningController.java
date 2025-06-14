package com.vn.fruitcart.controller.admin;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.CustomerCluster;
import com.vn.fruitcart.repository.CustomerClusterRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CustomerClusteringManagerService;

@Controller
@RequestMapping("/admin/ml")
public class AdminMachineLearningController {
    @Autowired
    private CustomerClusteringManagerService managerService;
    @Autowired
    private CustomerClusterRepository customerClusterRepository;

    @Autowired
    private BreadcrumbService breadcrumbService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Lấy thông tin về lần chạy cuối để hiển thị
        Optional<Instant> lastRunDate = customerClusterRepository.findFirstByOrderByAssignedDateDesc()
                .map(CustomerCluster::getAssignedDate);

        lastRunDate.ifPresent(date -> model.addAttribute("lastRunDate", date));

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminDetailProduct());
        return "admin/pages/ml/dashboard";
    }

    @PostMapping("/run-clustering")
    public String runClustering(RedirectAttributes redirectAttributes) {
        managerService.runClusteringProcess();
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã bắt đầu tác vụ phân cụm khách hàng. Quá trình có thể mất vài phút để hoàn thành và cập nhật.");
        return "redirect:/admin/ml/dashboard";
    }
}
