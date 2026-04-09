package com.example.lab05.service;

import com.example.lab05.dto.DashboardResponse;
import com.example.lab05.model.cassandra.SensorReading;
import com.example.lab05.model.mongo.PurchaseReceipt;
import com.example.lab05.repository.PurchaseReceiptRepository;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class DashboardService {

    private final PurchaseReceiptRepository receiptRepository;
    private final SocialGraphService socialGraphService;
    private final SensorService sensorService;
    private final ProductSearchService searchService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Logger log =
            LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(PurchaseReceiptRepository receiptRepository,
                            SocialGraphService socialGraphService,
                            SensorService sensorService,
                            ProductSearchService searchService,
                            RedisTemplate<String, Object> redisTemplate) {
        this.receiptRepository = receiptRepository;
        this.socialGraphService = socialGraphService;
        this.sensorService = sensorService;
        this.searchService = searchService;
        this.redisTemplate = redisTemplate;
    }

    public DashboardResponse getDashboard(String personName) {
        String key = "dashboard:" + personName;

        try {
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                DashboardResponse res = (DashboardResponse) cached;

                return new DashboardResponse(
                        res.personName(),
                        res.totalSpent(),
                        res.purchaseCount(),
                        res.recentPurchases(),
                        res.friendRecommendations(),
                        res.friendsOfFriends(),
                        res.recentActivity(),
                        res.youMightAlsoLike(),
                        true
                );
            }
        } catch (Exception e) {
            log.warn("Redis cache check failed for {}: {}", personName, e.getMessage());
        }
        List<PurchaseReceipt> receipts = receiptRepository.findByPersonName(personName);

        double totalSpent = receipts.stream()
                .mapToDouble(PurchaseReceipt::getTotalPrice)
                .sum();

        int purchaseCount = receipts.size();

        List<PurchaseReceipt> recentPurchases = receipts.stream()
                .sorted((a, b) -> b.getPurchasedAt().compareTo(a.getPurchasedAt()))
                .limit(5)
                .toList();

        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<String> friendsOfFriends = new ArrayList<>();

        try {
            recommendations = socialGraphService.getRecommendations(personName, 5);

            friendsOfFriends = socialGraphService.getFriendsOfFriends(personName)
                    .stream()
                    .map(p -> p.getName())
                    .toList();

        } catch (Exception e) {
            log.warn("Failed to fetch Neo4j data for {}: {}", personName, e.getMessage());
        }
        List<SensorReading> activity = new ArrayList<>();

        try {
            activity = sensorService.getLatestReadings(
                    "user-activity-" + personName.toLowerCase(),
                    10
            );
        } catch (Exception e) {
            log.warn("Failed to fetch activity for {}: {}", personName, e.getMessage());
        }
        List<String> suggestions = new ArrayList<>();

        try {
            Set<String> purchasedNames = receipts.stream()
                    .map(PurchaseReceipt::getProductName)
                    .collect(Collectors.toSet());

            Set<String> categories = receipts.stream()
                    .map(PurchaseReceipt::getProductCategory)
                    .collect(Collectors.toSet());

            for (String category : categories) {
                var products = searchService.getByCategory(category);

                products.stream()
                        .filter(p -> !purchasedNames.contains(p.getName()))
                        .limit(2)
                        .forEach(p -> suggestions.add(p.getName()));
            }

        } catch (Exception e) {
            log.warn("Failed to fetch ES suggestions for {}: {}", personName, e.getMessage());
        }
        DashboardResponse response = new DashboardResponse(
                personName,
                totalSpent,
                purchaseCount,
                recentPurchases,
                recommendations,
                friendsOfFriends,
                activity,
                suggestions,
                false
        );

        try {
            redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.warn("Failed to cache dashboard for {}: {}", personName, e.getMessage());
        }

        return response;

    }
}