package s55_3546.t_02.malak_yassin.service;

import s55_3546.t_02.malak_yassin.dto.PurchaseRequest;
import s55_3546.t_02.malak_yassin.model.mongo.PurchaseReceipt;
import s55_3546.t_02.malak_yassin.model.cassandra.SensorReading;
import s55_3546.t_02.malak_yassin.model.cassandra.SensorReadingKey;
import s55_3546.t_02.malak_yassin.repository.PurchaseReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PurchaseService {

    private final ProductService productService;
    private final PurchaseReceiptRepository receiptRepository;
    private final SocialGraphService socialGraphService;
    private final SensorService sensorService;
    private final ProductSearchService searchService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Logger log =
            LoggerFactory.getLogger(PurchaseService.class);

    // ✅ Constructor
    public PurchaseService(ProductService productService,
                           PurchaseReceiptRepository receiptRepository,
                           SocialGraphService socialGraphService,
                           SensorService sensorService,
                           ProductSearchService searchService,
                           RedisTemplate<String, Object> redisTemplate) {
        this.productService = productService;
        this.receiptRepository = receiptRepository;
        this.socialGraphService = socialGraphService;
        this.sensorService = sensorService;
        this.searchService = searchService;
        this.redisTemplate = redisTemplate;
    }

    public PurchaseReceipt executePurchase(PurchaseRequest request) {

        // ✅ Step 1 — PostgreSQL
        var product = productService.getProductById(request.productId());

        if (product.getStockQuantity() < request.quantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStockQuantity(
                product.getStockQuantity() - request.quantity()
        );

        productService.updateProduct(product.getId(), product);

        // ✅ Step 2 — MongoDB
        PurchaseReceipt receipt = new PurchaseReceipt(
                request.personName(),
                product.getName(),
                product.getCategory(),
                request.quantity(),
                product.getPrice(),
                request.purchaseDetails()
        );

        receipt = receiptRepository.save(receipt);

        // ⚠️ Step 3 — Neo4j
        try {
            socialGraphService.purchase(
                    request.personName(),
                    product.getName(),
                    request.quantity(),
                    product.getPrice()
            );
        } catch (Exception e) {
            log.warn("Failed to create PURCHASED edge for {} -> {}: {}",
                    request.personName(),
                    product.getName(),
                    e.getMessage());
        }

        // ⚠️ Step 4 — Cassandra (FIXED PROPERLY)
        try {
            SensorReading reading = new SensorReading();

            reading.setKey(new SensorReadingKey(
                    "user-activity-" + request.personName().toLowerCase(),
                    Instant.now()
            ));

            reading.setLocation(product.getName());

            sensorService.recordReading(reading);

        } catch (Exception e) {
            log.warn("Failed to log purchase event for {}: {}",
                    request.personName(),
                    e.getMessage());
        }

        // ⚠️ Step 5 — Elasticsearch
        try {
            if (product.getStockQuantity() == 0) {
                var results = searchService.searchByName(product.getName());

                if (!results.isEmpty()) {
                    var esProduct = results.get(0);
                    esProduct.setInStock(false);
                    searchService.saveProduct(esProduct);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update ES inStock for product {}: {}",
                    product.getId(),
                    e.getMessage());
        }

        // ⚠️ Step 6 — Redis
        try {
            String key = "dashboard:" + request.personName();
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to evict dashboard cache for {}: {}",
                    request.personName(),
                    e.getMessage());
        }

        return receipt;
    }
}