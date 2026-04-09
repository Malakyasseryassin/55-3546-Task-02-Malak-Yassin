package s55_3546.t_02.malak_yassin.dto;

import java.util.Map;

public record PurchaseRequest(
        String personName,
        Long productId,
        Integer quantity,
        Map<String, Object> purchaseDetails
) {}