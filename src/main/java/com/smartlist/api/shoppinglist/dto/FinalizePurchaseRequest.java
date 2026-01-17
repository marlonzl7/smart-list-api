package com.smartlist.api.shoppinglist.dto;

import java.util.List;

public record FinalizePurchaseRequest(
        List<PurchasedItemDTO> items
) {}
