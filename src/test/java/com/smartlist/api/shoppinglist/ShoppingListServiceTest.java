package com.smartlist.api.shoppinglist;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.dto.ShoppingListResponse;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemResponse;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import com.smartlist.api.shoppinglistitem.repository.ShoppingListItemRepository;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceTest {

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@email.com");
    }

    @Test
    void shouldReturnActiveShoppingListWithItems() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(10L);
        shoppingList.setActive(true);
        shoppingList.setUser(user);

        List<ShoppingListItemResponse> items = List.of(
                new ShoppingListItemResponse(1L, "Arroz", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN)
        );

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.findItemsByShoppingListId(10L))
                .thenReturn(items);

        ShoppingListResponse dto = shoppingListService.getActiveShoppingListByUser(user);

        assertEquals(10L, dto.shoppingListId());
        assertTrue(dto.isActive());
        assertEquals(1, dto.items().size());
    }

    @Test
    void shouldThrowExceptionWhenNoActiveShoppingList() {
        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.getActiveShoppingListByUser(user)
        );

        assertEquals("SL1001", ex.getCode());
    }

    @Test
    void shouldReturnShoppingListById() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(20L);
        shoppingList.setActive(false);
        shoppingList.setUser(user);

        when(shoppingListRepository.findByShoppingListIdAndUser(20L, user))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.findItemsByShoppingListId(20L))
                .thenReturn(List.of());

        ShoppingListResponse dto = shoppingListService.getShoppingListWithItems(20L, user);

        assertEquals(20L, dto.shoppingListId());
    }

    @Test
    void shouldThrowExceptionWhenShoppingListNotFound() {
        when(shoppingListRepository.findByShoppingListIdAndUser(99L, user))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.getShoppingListWithItems(99L, user)
        );

        assertEquals("SL1002", ex.getCode());
    }

    @Test
    void shouldCreateAndActivateShoppingList() {
        when(shoppingListRepository.save(any(ShoppingList.class)))
                .thenAnswer(i -> i.getArgument(0));

        ShoppingList result = shoppingListService.getOrActiveShoppingList(user);

        assertTrue(result.isActive());
        assertEquals(user, result.getUser());

        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    void shouldAddItemToShoppingListWhenNotExists() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setActive(true);
        shoppingList.setUser(user);

        Item item = new Item();
        item.setItemId(5L);

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.existsByShoppingListAndItem(shoppingList, item))
                .thenReturn(false);

        shoppingListService.addItemToShoppingList(item, user);

        verify(shoppingListItemRepository).save(any(ShoppingListItem.class));
    }

    @Test
    void shouldNotAddItemIfAlreadyInShoppingList() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setActive(true);
        shoppingList.setUser(user);

        Item item = new Item();
        item.setItemId(5L);

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.existsByShoppingListAndItem(shoppingList, item))
                .thenReturn(true);

        shoppingListService.addItemToShoppingList(item, user);

        verify(shoppingListItemRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingItem() {
        when(shoppingListItemRepository.findByIdAndShoppingList_User(1L, user))
                .thenReturn(Optional.empty());

        ShoppingListItemUpdateRequest request =
                new ShoppingListItemUpdateRequest(BigDecimal.ONE, BigDecimal.TEN);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.updateShoppingListItem(1L, user, request)
        );

        assertEquals("SL1004", ex.getCode());
    }

    @Test
    void shouldRecalculateSubtotalWhenQuantityChanges() {
        ShoppingListItem item = spy(new ShoppingListItem());
        item.setPurchasedQuantity(BigDecimal.ONE);
        item.setUnitaryPrice(BigDecimal.TEN);

        when(shoppingListItemRepository.findByShoppingListItemIdAndShoppingList_User(1L, user))
                .thenReturn(Optional.of(item));

        ShoppingListItemUpdateRequest request =
                new ShoppingListItemUpdateRequest(BigDecimal.TWO, BigDecimal.TEN);

        shoppingListService.updateShoppingListItem(1L, user, request);

        verify(item).recalculateSubtotal();
        verify(shoppingListItemRepository).save(item);
    }

    @Test
    void shouldDeleteShoppingListItem() {
        ShoppingListItem item = new ShoppingListItem();

        when(shoppingListItemRepository.findByShoppingListItemIdAndShoppingList_User(1L, user))
                .thenReturn(Optional.of(item));

        shoppingListService.deleteShoppingListItem(1L, user);

        verify(shoppingListItemRepository).delete(item);
    }

}
