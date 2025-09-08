package com.smartlist.api.shoppinglist.dto;

import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;

import java.util.List;

public record ShoppingListDTO(
        Long shoppingListId,
        boolean isActive,
        List<ShoppingListItemDTO> items
) {
}
