package s55_3546.t_02.malak_yassin.repository;

import s55_3546.t_02.malak_yassin.model.mongo.PurchaseReceipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PurchaseReceiptRepository
        extends MongoRepository<PurchaseReceipt, String> {

    List<PurchaseReceipt> findByPersonName(String personName);

    List<PurchaseReceipt> findByProductCategory(String category);
}