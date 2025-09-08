package com.smartlist.api.shoppinglist.dto;

import java.math.BigDecimal;

public record ShoppingListItemUpdateRequest(
        Long shoppingListItemId,
        BigDecimal purchasedQuantity,
        BigDecimal unitaryPrice
) {
}
