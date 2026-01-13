package com.smartlist.api.shoppinglist.dto;

import java.math.BigDecimal;

public record ShoppingListItemUpdateRequest(
        BigDecimal purchasedQuantity,
        BigDecimal unitaryPrice
) {
}
