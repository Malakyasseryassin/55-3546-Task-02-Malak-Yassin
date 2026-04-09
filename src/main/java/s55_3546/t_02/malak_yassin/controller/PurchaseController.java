package s55_3546.t_02.malak_yassin.controller;

import s55_3546.t_02.malak_yassin.dto.PurchaseRequest;
import s55_3546.t_02.malak_yassin.model.mongo.PurchaseReceipt;
import s55_3546.t_02.malak_yassin.repository.PurchaseReceiptRepository;
import s55_3546.t_02.malak_yassin.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/55-3546/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseReceiptRepository receiptRepository;

    // ✅ Constructor
    public PurchaseController(PurchaseService purchaseService,
                              PurchaseReceiptRepository receiptRepository) {
        this.purchaseService = purchaseService;
        this.receiptRepository = receiptRepository;
    }

    // ✅ POST — execute purchase
    @PostMapping
    public PurchaseReceipt purchase(@RequestBody PurchaseRequest request) {
        return purchaseService.executePurchase(request);
    }

    // ✅ GET — get all purchases for a person
    @GetMapping("/person/{personName}")
    public List<PurchaseReceipt> getByPerson(@PathVariable String personName) {
        return receiptRepository.findByPersonName(personName);
    }
}