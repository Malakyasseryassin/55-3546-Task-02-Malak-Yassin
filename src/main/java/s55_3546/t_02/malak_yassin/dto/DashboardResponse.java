package s55_3546.t_02.malak_yassin.dto;

import s55_3546.t_02.malak_yassin.model.cassandra.SensorReading;
import s55_3546.t_02.malak_yassin.model.mongo.PurchaseReceipt;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        String personName,
        Double totalSpent,
        Integer purchaseCount,
        List<PurchaseReceipt> recentPurchases,
        List<Map<String, Object>> friendRecommendations,
        List<String> friendsOfFriends,
        List<SensorReading> recentActivity,
        List<String> youMightAlsoLike,
        boolean servedFromCache
) implements Serializable {}