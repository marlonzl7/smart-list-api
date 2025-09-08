package com.smartlist.api.shoppinglist.dto;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.model.ShoppingList;

import java.math.BigDecimal;

public record ShoppingListItemRegisterRequest(
        Long shoppingListId,
        Long itemId,
        BigDecimal purchasedQuantity,
        BigDecimal unitaryPrice
) {
}
