package com.vn.fruitcart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClusteringScheduledTask {
    @Autowired
    private CustomerClusteringManagerService managerService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void runCustomerClustering() {
        managerService.runClusteringProcess();
    }
}
