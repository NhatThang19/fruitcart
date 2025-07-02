package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.CustomerCluster;
import com.vn.fruitcart.entity.dto.request.UserClusteringData;
import com.vn.fruitcart.repository.CustomerClusterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class CustomerClusteringManagerService {

    @Autowired
    private UserService userService;
    @Autowired
    private CustomerClusteringService clusteringService;
    @Autowired
    private CustomerClusterRepository customerClusterRepository;

    @Async
    @Transactional
    public void runClusteringProcess() {
        log.info("Bắt đầu tác vụ phân cụm khách hàng vào lúc: {}", Instant.now());

        List<UserClusteringData> dataForClustering = userService.getAllUsersClusteringData();
        if (dataForClustering.isEmpty()) {
            log.info("Không có dữ liệu khách hàng để phân cụm. Kết thúc.");
            return;
        }
        log.info("Đã tính toán xong dữ liệu cho {} khách hàng.", dataForClustering.size());

        try {
            List<UserClusteringData> clusteredData = clusteringService.performClustering(dataForClustering);
            log.info("Đã thực hiện phân cụm thành công.");

            for (UserClusteringData data : clusteredData) {
                CustomerCluster cluster = customerClusterRepository.findByUser(data.getUser())
                        .orElse(new CustomerCluster());

                cluster.setUser(data.getUser());
                cluster.setClusterNumber(data.getAssignedCluster());
                cluster.setAssignedDate(Instant.now());

                data.getUser().setCustomerCluster(cluster);

                customerClusterRepository.save(cluster);
            }
            log.info("Đã cập nhật kết quả phân cụm cho {} khách hàng vào database.", clusteredData.size());

        } catch (Exception e) {
            log.error("Đã xảy ra lỗi trong quá trình phân cụm khách hàng.", e);
        }

        log.info("Kết thúc tác vụ phân cụm khách hàng.");
    }
}