package s55_3546.t_02.malak_yassin.controller;

// ═══════════════════════════════════════════════════════════════
// TODO (Section 2 — MongoDB):
// Uncomment this entire controller after implementing
// MongoProductService and MongoProductRepository.
// ═══════════════════════════════════════════════════════════════

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import s55_3546.t_02.malak_yassin.dto.CategoryAvgDTO;
import s55_3546.t_02.malak_yassin.model.mongo.MongoProduct;
import s55_3546.t_02.malak_yassin.service.MongoProductService;

@RestController
@RequestMapping("/api/mongo/products")
public class MongoProductController {

    private final MongoProductService mongoProductService;

    public MongoProductController(MongoProductService mongoProductService) {
        this.mongoProductService = mongoProductService;
    }

    @GetMapping
    public List<MongoProduct> getAll() {
        return mongoProductService.getAllProducts();
    }

    @GetMapping("/{id}")
    public MongoProduct getById(@PathVariable String id) {
        return mongoProductService.getProductById(id);
    }

    @PostMapping
    public MongoProduct create(@RequestBody MongoProduct product) {
        return mongoProductService.createProduct(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        mongoProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public List<MongoProduct> byCategory(@PathVariable String category) {
        return mongoProductService.getByCategory(category);
    }

    @GetMapping("/price")
    public List<MongoProduct> byPrice(@RequestParam Double min, @RequestParam Double max) {
        return mongoProductService.getByPriceRange(min, max);
    }

    @GetMapping("/tag/{tag}")
    public List<MongoProduct> byTag(@PathVariable String tag) {
        return mongoProductService.getByTag(tag);
    }

    @GetMapping("/analytics/category-avg")
    public List<CategoryAvgDTO> categoryAverages() {
        return mongoProductService.getAverageByCategory();
    }
}
