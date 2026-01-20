package com.smartlist.api.shoppinglist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PurchasedItemRequest(
        @NotNull(message = "Item da lista é obrigatório.")
        Long shoppingListItemId,

        @NotNull(message = "Quantidade comprada é obrigatória.")
        @Positive(message = "Quantidade deve ser positiva")
        BigDecimal purchasedQuantity,

        @PositiveOrZero(message = "Preço unitário não pode ser negativo.")
        BigDecimal unitaryPrice
) {}
