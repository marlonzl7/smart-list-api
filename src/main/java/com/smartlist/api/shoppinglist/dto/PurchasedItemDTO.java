package com.smartlist.api.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchasedItemDTO(
        @NotNull Long shoppingListItemId,
        @NotNull BigDecimal purchasedQuantity,
        BigDecimal unitaryPrice
) {}
