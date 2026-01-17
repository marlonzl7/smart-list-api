package com.smartlist.api.shoppinglist.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.item.service.ItemService;
import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.shoppinglist.dto.FinalizePurchaseRequest;
import com.smartlist.api.shoppinglist.dto.PurchasedItemDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShoppingListApplicationService {

    private final ShoppingListService shoppingListService;
    private final InventoryApplicationService inventoryApplicationService;
    private final ShoppingListRepository shoppingListRepository;
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    public ShoppingListApplicationService(
            ShoppingListService shoppingListService,
            InventoryApplicationService inventoryApplicationService,
            ShoppingListRepository shoppingListRepository,
            ItemService itemService,
            ItemRepository itemRepository
    ) {
        this.shoppingListService = shoppingListService;
        this.inventoryApplicationService = inventoryApplicationService;
        this.shoppingListRepository = shoppingListRepository;
        this.itemService = itemService;
        this.itemRepository = itemRepository;
    }

    public ShoppingListDTO getActiveShoppingList(User user) {
        ShoppingListDTO dto = shoppingListService.getActiveShoppingListByUser(user);

        inventoryApplicationService.onItemUpdated(user);

        return dto;
    }

    @Transactional
    public boolean finalizeShoppingList(Long shoppingListId, FinalizePurchaseRequest finalizePurchaseRequestDTO, User user) {
        log.info("Iniciando processo de finalização de compra para usuário ID: {}", user.getUserId());

        Map<Long, PurchasedItemDTO> purchasedItems = new HashMap<>();

        for (PurchasedItemDTO item : finalizePurchaseRequestDTO.items()) {
            purchasedItems.put(item.shoppingListItemId(), item);
        }

        ShoppingList shoppingList = shoppingListRepository
                .findByShoppingListIdAndUserAndActiveTrue(shoppingListId, user)
                .orElseThrow(() -> {
                    log.error("Tentativa de finalização de compra com lista de compra inexistente ou inativa");
                    return new BadRequestException("SLA1001", "Lista inexistente ou inativa");
                });

        List<ShoppingListItem> shoppingListItems = shoppingList.getItems();

        for (ShoppingListItem shoppingListItem : shoppingListItems) {

            PurchasedItemDTO dto = purchasedItems.get(shoppingListItem.getShoppingListItemId());

            if (dto != null) {

                if (dto.purchasedQuantity().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException("SLA1002", "Quantidade inválida");
                }

                if (dto.unitaryPrice() == null || dto.unitaryPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException("SLA1003", "Preço unitário inválido");
                }

                shoppingListItem.setPurchasedQuantity(dto.purchasedQuantity());
                shoppingListItem.setUnitaryPrice(dto.unitaryPrice());
                shoppingListItem.setSubtotal(dto.purchasedQuantity().multiply(dto.unitaryPrice()));

                inventoryApplicationService.addStock(user, shoppingListItem.getItem(), dto.purchasedQuantity());

            } else {
                shoppingListItem.setPurchasedQuantity(BigDecimal.ZERO);
                shoppingListItem.setUnitaryPrice(null);
                shoppingListItem.setSubtotal(BigDecimal.ZERO);
            }

        }

        shoppingList.setActive(false);
        shoppingListRepository.save(shoppingList);

        return true;
    }
}
