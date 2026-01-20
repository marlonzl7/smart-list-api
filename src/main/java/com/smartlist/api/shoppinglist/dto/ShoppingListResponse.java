package com.smartlist.api.shoppinglist.dto;

import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemResponse;

import java.util.List;

public record ShoppingListResponse(
        Long shoppingListId,
        boolean isActive,
        List<ShoppingListItemResponse> items
) {
}
