package com.smartlist.api.inventory.service;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.user.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryApplicationService {

    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    public InventoryApplicationService(ItemRepository itemRepository, InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.inventoryService = inventoryService;
    }

    public void onItemUpdated(User user) {
        List<Item> items = itemRepository.findByUser(user);
        items.forEach(inventoryService::processItem);
    }
}
