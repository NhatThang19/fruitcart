package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.dto.request.UserClusteringData;
import com.vn.fruitcart.util.constant.CustomerClusterEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(CustomerClusteringService.class);

    private record CentroidInfo(int wekaIndex, Instance centroid) {
    }

    public List<UserClusteringData> performClustering(List<UserClusteringData> userDataList) throws Exception {
        if (userDataList.isEmpty()) {
            logger.warn("Danh sách người dùng để phân cụm trống. Bỏ qua.");
            return new ArrayList<>();
        }

        logger.info("Bắt đầu quá trình phân cụm cho {} người dùng.", userDataList.size());

        ArrayList<Attribute> attributes = createWekaAttributes();
        Instances wekaData = createWekaInstances(userDataList, attributes);

        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setSeed(10);
        kMeans.setPreserveInstancesOrder(true);
        kMeans.setNumClusters(4);
        kMeans.buildClusterer(wekaData);

        logger.info("Thuật toán K-Means đã chạy xong. Bắt đầu gán nhãn cho các cụm.");
        Map<Integer, CustomerClusterEnum> wekaIndexToEnumMap = mapCentroidsToEnum(kMeans.getClusterCentroids(),
                attributes);

        for (int i = 0; i < wekaData.numInstances(); i++) {
            int wekaClusterIndex = kMeans.clusterInstance(wekaData.instance(i));
            CustomerClusterEnum mappedEnum = wekaIndexToEnumMap.getOrDefault(wekaClusterIndex,
                    CustomerClusterEnum.UNKNOWN);
            userDataList.get(i).setAssignedCluster(mappedEnum.getClusterNumber());
        }

        logger.info("Hoàn tất quá trình phân cụm.");
        return userDataList;
    }

    private Map<Integer, CustomerClusterEnum> mapCentroidsToEnum(Instances centroids, ArrayList<Attribute> attributes) {
        Map<Integer, CustomerClusterEnum> mapping = new HashMap<>();
        List<CentroidInfo> centroidInfos = new ArrayList<>();

        logger.info("-------------------- THÔNG TIN TÂM CỤM (CENTROIDS) --------------------");
        for (int i = 0; i < centroids.numInstances(); i++) {
            Instance centroid = centroids.instance(i);
            centroidInfos.add(new CentroidInfo(i, centroid));
            logger.info("Tâm cụm (Weka Index {}): Spending={}, Orders={}, AOV={}, Freq={}, Recency={}",
                    i,
                    String.format("%.2f", centroid.value(0)),
                    String.format("%.2f", centroid.value(1)),
                    String.format("%.2f", centroid.value(2)),
                    String.format("%.2f", centroid.value(3)),
                    String.format("%.2f", centroid.value(4))
            );
        }
        logger.info("--------------------------------------------------------------------");


        Attribute spendingAttr = attributes.get(0);
        centroidInfos.sort(Comparator.comparingDouble(info -> ((CentroidInfo) info).centroid().value(spendingAttr)).reversed());

        int numClusters = centroidInfos.size();
        logger.info("Sắp xếp các tâm cụm theo chi tiêu (Total Spending) giảm dần.");

        if (numClusters >= 1) {
            mapping.put(centroidInfos.get(0).wekaIndex(), CustomerClusterEnum.GOLD_CORE);
        }
        if (numClusters >= 2) {
            mapping.put(centroidInfos.get(1).wekaIndex(), CustomerClusterEnum.DEVELOPING);
        }

        if (numClusters == 3) {
            mapping.put(centroidInfos.get(2).wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
        } else if (numClusters >= 4) {
            Attribute recencyAttr = attributes.get(4);
            CentroidInfo lowSpending1 = centroidInfos.get(2);
            CentroidInfo lowSpending2 = centroidInfos.get(3);

            logger.info("Phân loại 2 cụm chi tiêu thấp dựa trên Recency:");
            logger.info(" - Cụm A: Recency = {}", String.format("%.2f", lowSpending1.centroid().value(recencyAttr)));
            logger.info(" - Cụm B: Recency = {}", String.format("%.2f", lowSpending2.centroid().value(recencyAttr)));

            if (lowSpending1.centroid().value(recencyAttr) > lowSpending2.centroid().value(recencyAttr)) {
                mapping.put(lowSpending1.wekaIndex(), CustomerClusterEnum.NEW_OR_THRIFTY);
                mapping.put(lowSpending2.wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
            } else {
                mapping.put(lowSpending2.wekaIndex(), CustomerClusterEnum.NEW_OR_THRIFTY);
                mapping.put(lowSpending1.wekaIndex(), CustomerClusterEnum.INFREQUENT_VISITOR);
            }
        }

        logger.info("-------------------- KẾT QUẢ GÁN NHÃN --------------------");
        mapping.forEach((wekaIndex, enumValue) -> {
            logger.info("Tâm cụm (Weka Index {}) => {}", wekaIndex, enumValue.name());
        });
        logger.info("----------------------------------------------------------");


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