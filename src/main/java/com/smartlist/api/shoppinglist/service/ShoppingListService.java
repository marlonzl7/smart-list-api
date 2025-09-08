package com.smartlist.api.shoppinglist.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemRegisterRequest;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import com.smartlist.api.shoppinglistitem.repository.ShoppingListItemRepository;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ItemRepository itemRepository;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingListItemRepository shoppingListItemRepository, ItemRepository itemRepository) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.itemRepository = itemRepository;
    }

    public ShoppingListDTO getActiveShoppingListByUser(User user) {
        ShoppingList shoppingList = shoppingListRepository.findByUserAndActiveTrue(user).orElseThrow(() -> {
            log.error("Tentativa de obter lista ativa inexistente");
            return new BadRequestException("SL1001", "Lista inexistente");
        });

        List<ShoppingListItemDTO> items = shoppingListItemRepository.findItemsByShoppingListId(shoppingList.getShoppingListId());

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

    public void registerShoppingListItem(ShoppingListItemRegisterRequest itemDTO, User user) {
        log.info("Iniciando tentativa de cadastro de item na lista de compras");

        ShoppingList shoppingList = shoppingListRepository.findByUserAndActiveTrue(user).orElseGet(() -> {
            log.info("Nenhuma lista de compras ativa encontrada para usuário: " + user.getEmail() + ". Criando nova lista...");
            return createAndActiveShoppingList(user);
        });

        ShoppingListItem shoppingListItem = new ShoppingListItem();
        shoppingListItem.setShoppingList(shoppingList);

        Item item = itemRepository.findByUserAndItemId(user, itemDTO.itemId()).orElseThrow(() -> {
            log.error("Tentativa de adição de item inexistente na lista de compras");
            return new BadRequestException("SL1003", "Item inexistente");
        });

        shoppingListItem.setItem(item);
        shoppingListItem.setPurchasedQuantity(itemDTO.purchasedQuantity());
        shoppingListItem.setUnitaryPrice(itemDTO.unitaryPrice());
        shoppingListItem.recalculateSubtotal();

        shoppingListItemRepository.save(shoppingListItem);

        log.info("Cadastro de item na lista de compras realizado com sucesso.");
    }

    public void updateShoppingListItem(ShoppingListItemUpdateRequest itemDTO) {
        log.info("Iniciando tentativa de atualização de item da lista de compras");

        ShoppingListItem item = shoppingListItemRepository.findById(itemDTO.shoppingListItemId()).orElseThrow(() -> {
            log.error("Tentativa de atualização de item de lista de compras inexistente");
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

        log.info("Item da lista de compras atualizado com sucesso.");
    }

    public void deleteShoppingListItemById(Long shoppingListItemId) {
        log.info("Iniciando tentativa de exclusão de item");

        ShoppingListItem item = shoppingListItemRepository.findById(shoppingListItemId).orElseThrow(() -> {
            log.error("Tentativa de exclusão de item inexistente.");
            return new BadRequestException("SL1005", "Item inexistente");
        });

        shoppingListItemRepository.deleteById(shoppingListItemId);

        log.info("Item de lista de compras excluído com sucesso");
    }
}
