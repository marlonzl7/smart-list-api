package com.smartlist.api.shoppinglist.service;

import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class ShoppingListApplicationService {

    private final ShoppingListService shoppingListService;
    private final InventoryApplicationService inventoryApplicationService;

    public ShoppingListApplicationService(ShoppingListService shoppingListService, InventoryApplicationService inventoryApplicationService) {
        this.shoppingListService = shoppingListService;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    public ShoppingListDTO getActiveShoppingList(User user) {
        ShoppingListDTO dto = shoppingListService.getActiveShoppingListByUser(user);

        inventoryApplicationService.onItemUpdated(user);

        return dto;
    }
}
