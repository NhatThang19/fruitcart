package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.dto.request.UserClusteringData;
import com.vn.fruitcart.util.constant.CustomerClusterEnum;
import org.springframework.stereotype.Service;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerClusteringService {

    private record CentroidInfo(int wekaIndex, Instance centroid) {
    }

    public List<UserClusteringData> performClustering(List<UserClusteringData> userDataList) throws Exception {
        if (userDataList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Attribute> attributes = createWekaAttributes();
        Instances wekaData = createWekaInstances(userDataList, attributes);

        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setSeed(10);
        kMeans.setPreserveInstancesOrder(true);
        kMeans.setNumClusters(4);
        kMeans.buildClusterer(wekaData);

        Map<Integer, CustomerClusterEnum> wekaIndexToEnumMap = mapCentroidsToEnum(kMeans.getClusterCentroids(),
                attributes);

        for (int i = 0; i < wekaData.numInstances(); i++) {
            int wekaClusterIndex = kMeans.clusterInstance(wekaData.instance(i));
            CustomerClusterEnum mappedEnum = wekaIndexToEnumMap.getOrDefault(wekaClusterIndex,
                    CustomerClusterEnum.UNKNOWN);
            userDataList.get(i).setAssignedCluster(mappedEnum.getClusterNumber());
        }

        return userDataList;
    }

    /**
     * PHIÊN BẢN MỚI: Xử lý linh hoạt số lượng cụm trả về
     */
    private Map<Integer, CustomerClusterEnum> mapCentroidsToEnum(Instances centroids, ArrayList<Attribute> attributes) {
        Map<Integer, CustomerClusterEnum> mapping = new HashMap<>();
        List<CentroidInfo> centroidInfos = new ArrayList<>();

        for (int i = 0; i < centroids.numInstances(); i++) {
            centroidInfos.add(new CentroidInfo(i, centroids.instance(i)));
        }

        // Sắp xếp các cụm dựa trên Tổng chi tiêu (Total_Spending) giảm dần
        Attribute spendingAttr = attributes.get(0);
        centroidInfos.sort(Comparator.comparingDouble(info -> ((CentroidInfo) info).centroid().value(spendingAttr)).reversed());

        int numClusters = centroidInfos.size();

        // Gán nhãn động dựa trên số cụm thực tế có được
        if (numClusters >= 1) {
            // Cụm chi tiêu cao nhất luôn là KHÁCH HÀNG VÀNG
            mapping.put(centroidInfos.get(0).wekaIndex(), CustomerClusterEnum.GOLD_CORE);
        }
        if (numClusters >= 2) {
            // Cụm chi tiêu cao thứ hai là KHÁCH HÀNG PHÁT TRIỂN
            mapping.put(centroidInfos.get(1).wekaIndex(), CustomerClusterEnum.DEVELOPING);
        }

        // Xử lý 2 cụm chi tiêu thấp (nếu tồn tại)
        if (numClusters == 3) {
            // Nếu chỉ còn 1 cụm, ưu tiên gán là KHÁCH HÀNG ÍT GIAO DỊCH
            mapping.put(centroidInfos.get(2).wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
        } else if (numClusters >= 4) {
            // Nếu có đủ 2 cụm chi tiêu thấp, phân biệt bằng Recency
            Attribute recencyAttr = attributes.get(4);
            CentroidInfo lowSpending1 = centroidInfos.get(2);
            CentroidInfo lowSpending2 = centroidInfos.get(3);

            if (lowSpending1.centroid().value(recencyAttr) > lowSpending2.centroid().value(recencyAttr)) {
                // Cụm có Recency cao hơn (lâu không mua hơn) -> KHÁCH HÀNG MỚI/TIẾT KIỆM
                mapping.put(lowSpending1.wekaIndex(), CustomerClusterEnum.NEW_OR_THRIFTY);
                mapping.put(lowSpending2.wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
            } else {
                mapping.put(lowSpending2.wekaIndex(), CustomerClusterEnum.NEW_OR_THRIFTY);
                mapping.put(lowSpending1.wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
            }
        }

        return mapping;
    }

    private ArrayList<Attribute> createWekaAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("Total_Spending"));
        attributes.add(new Attribute("Total_Orders"));
        attributes.add(new Attribute("Average_Order_Value"));
        attributes.add(new Attribute("Recent_Purchase_Frequency_90_Days"));
        attributes.add(new Attribute("Recency_Days_Since_Last_Purchase"));
        return attributes;
    }

    private Instances createWekaInstances(List<UserClusteringData> userDataList, ArrayList<Attribute> attributes) {
        Instances wekaData = new Instances("CustomerData", attributes, userDataList.size());
        for (UserClusteringData userData : userDataList) {
            double[] values = new double[wekaData.numAttributes()];
            values[0] = userData.getTotalSpending().doubleValue();
            values[1] = userData.getTotalOrders();
            values[2] = userData.getAverageOrderValue().doubleValue();
            values[3] = userData.getPurchaseFrequencyLast90Days();
            values[4] = userData.getDaysSinceLastPurchase() != null ? userData.getDaysSinceLastPurchase() : 365;
            wekaData.add(new DenseInstance(1.0, values));
        }
        return wekaData;
    }
}