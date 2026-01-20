package com.smartlist.api.shoppinglist.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.service.InventoryService;
import com.smartlist.api.shoppinglist.dto.ShoppingListResponse;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemResponse;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import com.smartlist.api.shoppinglistitem.repository.ShoppingListItemRepository;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    public ShoppingListService(
            ShoppingListRepository shoppingListRepository,
            ShoppingListItemRepository shoppingListItemRepository
    ) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
    }

    public ShoppingListResponse getActiveShoppingListByUser(User user) {
        ShoppingList shoppingList = getActiveShoppingList(user);
        return buildResponse(shoppingList);
    }

    public ShoppingListResponse getShoppingListWithItems(Long shoppingListId, User user) {
        ShoppingList shoppingList = shoppingListRepository
                .findByShoppingListIdAndUser(shoppingListId, user)
                .orElseThrow(() -> new BadRequestException("SL1002", "Lista inexistente"));

        return buildResponse(shoppingList);
    }

    public ShoppingList getOrActiveShoppingList(User user) {
        return shoppingListRepository
                .findByUserAndActiveTrue(user)
                .orElseGet(() -> createActiveShoppingList(user));
    }

    private ShoppingList createActiveShoppingList(User user) {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setUser(user);
        shoppingList.setActive(true);

        shoppingListRepository.save(shoppingList);

        log.info("Nova lista de compras criada para usuário ID: {}", user.getUserId());
        return shoppingList;
    }

    public void addItemToShoppingList(Item item, User user) {
        ShoppingList shoppingList = getOrActiveShoppingList(user);

        boolean alreadyExists =
                shoppingListItemRepository.existsByShoppingListAndItem(shoppingList, item);

        if (alreadyExists) {
            log.debug("Item ID {} já existe na lista de compras do usuário {}", item.getItemId(), user.getUserId());
            return;
        }

        ShoppingListItem shoppingListItem = ShoppingListItem.create(item, shoppingList);
        shoppingListItemRepository.save(shoppingListItem);

        log.info("Item ID {} adicionado a lista do usuário ID {}", item.getItemId(), user.getUserId());
    }

    public void updateShoppingListItem(Long shoppingListItemId, User user, ShoppingListItemUpdateRequest dto) {
        ShoppingListItem item = shoppingListItemRepository
                .findByIdAndShoppingList_User(shoppingListItemId, user)
                .orElseThrow(() -> new BadRequestException("SL1004", "Item inexistente"));

        boolean changed = false;

        if (
                dto.purchasedQuantity() != null &&
                dto.purchasedQuantity().compareTo(item.getPurchasedQuantity()) != 0
        ) {
            item.setPurchasedQuantity(dto.purchasedQuantity());
            changed = true;
        }

        if (
                dto.unitaryPrice() != null &&
                dto.unitaryPrice().compareTo(item.getUnitaryPrice()) != 0
        ) {
            item.setUnitaryPrice(dto.unitaryPrice());
            changed = true;
        }

        if (changed) {
            item.recalculateSubtotal();
            shoppingListItemRepository.save(item);
            log.info("Item ID {} atualizado para usuário ID {}", shoppingListItemId, user.getUserId());
        }
    }

    public void deleteShoppingListItem(Long shoppingListItemId, User user) {
        ShoppingListItem item = shoppingListItemRepository
                .findByIdAndShoppingList_User(shoppingListItemId, user)
                .orElseThrow(() -> new BadRequestException("SL1005", "Item inexistente"));

        shoppingListItemRepository.delete(item);

        log.info("Item ID {} removido da lista do usuário ID {}", shoppingListItemId, user.getUserId());
    }

    private ShoppingList getActiveShoppingList(User user) {
        return shoppingListRepository
                .findByUserAndActiveTrue(user)
                .orElseThrow(() -> new BadRequestException("SL1001", "Lista ativa inexistente"));
    }

    private ShoppingListResponse buildResponse(ShoppingList shoppingList) {
        List<ShoppingListItemResponse> items =
                shoppingListItemRepository.findItemsByShoppingListId(
                        shoppingList.getShoppingListId()
                );

        return new ShoppingListResponse(
                shoppingList.getShoppingListId(),
                shoppingList.isActive(),
                items
        );
    }
}
