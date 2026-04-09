package s55_3546.t_02.malak_yassin.repository.mongo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import s55_3546.t_02.malak_yassin.dto.CategoryAvgDTO;

// Pattern 3: MongoTemplate -- programmatic queries
@Repository
public class MongoAggregationRepository {

    private final MongoTemplate mongoTemplate;

    public MongoAggregationRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryAvgDTO> getAverageByCategory() {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("rating").gt(4.0)),
            Aggregation.group("category").avg("price").as("avgPrice"),
            Aggregation.sort(Sort.Direction.DESC, "avgPrice"),
            Aggregation.limit(5),
            // $group outputs "_id" for the grouped field.
            // $project renames it back to "category" so it matches the record field name.
            Aggregation.project("avgPrice").and("_id").as("category")
        );
        AggregationResults<CategoryAvgDTO> results =
            mongoTemplate.aggregate(agg, "products", CategoryAvgDTO.class);
        return results.getMappedResults();
    }
}