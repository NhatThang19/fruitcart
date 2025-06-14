package com.vn.fruitcart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.dto.request.UserClusteringData;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

@Service
public class CustomerClusteringService {
    public List<UserClusteringData> performClustering(List<UserClusteringData> userDataList) throws Exception {
        if (userDataList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Tạo header cho Weka Instances
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("Total_Spending"));
        attributes.add(new Attribute("Total_Orders"));
        attributes.add(new Attribute("Average_Order_Value"));
        attributes.add(new Attribute("Recent_Purchase_Frequency_90_Days"));
        attributes.add(new Attribute("Recency_Days_Since_Last_Purchase"));

        Instances wekaData = new Instances("CustomerData", attributes, userDataList.size());

        // 2. Chuyển đổi dữ liệu từ DTO sang Weka Instances
        for (UserClusteringData userData : userDataList) {
            double[] values = new double[wekaData.numAttributes()];
            values[0] = userData.getTotalSpending().doubleValue();
            values[1] = userData.getTotalOrders();
            values[2] = userData.getAverageOrderValue().doubleValue();
            values[3] = userData.getPurchaseFrequencyLast90Days();
            values[4] = userData.getDaysSinceLastPurchase() != null ? userData.getDaysSinceLastPurchase() : 365; 
            wekaData.add(new DenseInstance(1.0, values));
        }

        // 3. Cấu hình và chạy thuật toán K-Means
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setSeed(10); // Đặt seed để kết quả có thể tái lặp
        kMeans.setPreserveInstancesOrder(true);
        kMeans.setNumClusters(4); // Thiết lập số cụm như bạn yêu cầu
        kMeans.buildClusterer(wekaData);

        // 4. Gán nhãn cụm cho từng khách hàng
        for (int i = 0; i < wekaData.numInstances(); i++) {
            int cluster = kMeans.clusterInstance(wekaData.instance(i));
            userDataList.get(i).setAssignedCluster(cluster);
        }

        return userDataList;
    }
}
