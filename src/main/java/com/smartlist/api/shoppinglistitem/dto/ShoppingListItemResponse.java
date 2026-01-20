package com.smartlist.api.shoppinglistitem.dto;

import java.math.BigDecimal;

public record ShoppingListItemResponse(
        Long shoppingListItemId,
        String itemName,
        BigDecimal purchasedQuantity,
        BigDecimal unitaryPrice,
        BigDecimal subtotal
) {
}
