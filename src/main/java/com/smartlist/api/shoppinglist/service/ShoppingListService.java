package com.smartlist.api.shoppinglist.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.service.InventoryService;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO;
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
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingListItemRepository shoppingListItemRepository, ItemRepository itemRepository, InventoryService inventoryService) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.itemRepository = itemRepository;
        this.inventoryService = inventoryService;
    }

    public ShoppingListDTO getActiveShoppingListByUser(User user) {
        ShoppingList shoppingList = shoppingListRepository.findByUserAndActiveTrue(user).orElseThrow(() -> {
            log.error("Tentativa de obter lista ativa inexistente");
            return new BadRequestException("SL1001", "Lista inexistente");
        });

        List<ShoppingListItemDTO> items = shoppingListItemRepository
                .findItemsByShoppingListId(shoppingList.getShoppingListId());

        return new ShoppingListDTO(
                shoppingList.getShoppingListId(),
                shoppingList.isActive(),
                items
        );
    }

    public ShoppingListDTO getShoppingListWithItems(Long shoppingListId) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(() -> {
            log.error("Tentativa de obter lista inexistente");
            return new BadRequestException("SL1002", "Lista inexistente");
        });

        List<ShoppingListItemDTO> items = shoppingListItemRepository.findItemsByShoppingListId(shoppingListId);

        return new ShoppingListDTO(
                shoppingList.getShoppingListId(),
                shoppingList.isActive(),
                items
        );
    }

    public ShoppingList createAndActiveShoppingList(User user) {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setUser(user);
        shoppingList.setActive(true);

        shoppingListRepository.save(shoppingList);

        log.info("Nova lista de compras para usuário: email " + user.getEmail());

        return shoppingList;
    }

    public void addItemToShoppingList(Item item, User user) {
        log.info("Iniciando tentativa de cadastro de item na lista de compras. Item ID: {}.", item.getItemId());

        ShoppingList shoppingList = shoppingListRepository.findByUserAndActiveTrue(user).orElseGet(() -> {
            log.info("Nenhuma lista de compras ativa encontrada para usuário: " + user.getEmail() + ". Criando nova lista...");
            return createAndActiveShoppingList(user);
        });

        ShoppingListItem shoppingListItem = ShoppingListItem.create(item, shoppingList);

        boolean alreadyInList = shoppingListItemRepository.existsByShoppingListAndItem(shoppingList, item);

        if (alreadyInList) {
            log.info("Item {} já está na lista de compras", item.getItemId());
            return;
        }

        shoppingListItemRepository.save(shoppingListItem);

        log.info("Cadastro de item na lista de compras realizado com sucesso. Item ID: {}.", item.getItemId());
    }

    public void updateShoppingListItem(ShoppingListItemUpdateRequest itemDTO) {
        log.info("Iniciando tentativa de atualização de item da lista de compras. Item ID: {}", itemDTO.shoppingListItemId());

        ShoppingListItem item = shoppingListItemRepository.findById(itemDTO.shoppingListItemId()).orElseThrow(() -> {
            log.error("Tentativa de atualização de item de lista de compras inexistente. Item ID: {}", itemDTO.shoppingListItemId());
            return new BadRequestException("SL1004", "Item inexistente");
        });

        boolean changed = false;

        if (!itemDTO.purchasedQuantity().equals(item.getPurchasedQuantity())) {
            item.setPurchasedQuantity(itemDTO.purchasedQuantity());
            changed = true;
        }

        if (!itemDTO.unitaryPrice().equals(item.getUnitaryPrice())) {
            item.setUnitaryPrice(itemDTO.unitaryPrice());
            changed = true;
        }

        if (changed) {
            item.recalculateSubtotal();
        }

        shoppingListItemRepository.save(item);

        log.info("Item da lista de compras atualizado com sucesso. Item ID: {}", itemDTO.shoppingListItemId());
    }

    public void deleteShoppingListItemById(Long shoppingListItemId) {
        log.info("Iniciando tentativa de exclusão de item. ID: {}", shoppingListItemId);

        ShoppingListItem item = shoppingListItemRepository.findById(shoppingListItemId).orElseThrow(() -> {
            log.error("Tentativa de exclusão de item inexistente. ID: {}", shoppingListItemId);
            return new BadRequestException("SL1005", "Item inexistente");
        });

        shoppingListItemRepository.deleteById(shoppingListItemId);

        log.info("Item de lista de compras excluído com sucesso. ID: {}", shoppingListItemId);
    }
}
