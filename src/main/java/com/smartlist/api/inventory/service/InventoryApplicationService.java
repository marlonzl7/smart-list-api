package com.smartlist.api.inventory.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class InventoryApplicationService {

    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    public InventoryApplicationService(ItemRepository itemRepository, InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.inventoryService = inventoryService;
    }

    public void onItemUpdated(Item item) {
        inventoryService.processItem(item);
    }

    public void refreshInventory(User user) {
        log.info("Atualização completa de inventário iniciada. UserId={}", user.getUserId());

        List<Item> items = itemRepository.findByUser(user);
        items.forEach(inventoryService::processItem);

        log.info("Atualização completa de inventário finalizada. UserId={}", user.getUserId());
    }

    public void addStock(User user, Item item, BigDecimal quantity) {
        log.info(
                "Adição de estoque iniciada. UserId={}, ItemId={}, Quantity={}",
                user.getUserId(),
                item.getItemId(),
                quantity
        );

        if (!item.getUser().equals(user)) {
            throw new BadRequestException("IA2002", "Acesso indevido ao item");
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("IA2001", "Quantidade deve ser maior que zero");
        }

        item.setQuantity(item.getQuantity().add(quantity));
        item.setLastStockUpdate(LocalDate.now());

        itemRepository.save(item);
        inventoryService.processItem(item);

        log.info("Estoque atualizado com sucesso. ItemId={}", item.getItemId());
    }
}
