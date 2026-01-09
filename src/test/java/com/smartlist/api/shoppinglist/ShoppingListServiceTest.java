package com.smartlist.api.shoppinglist;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemRegisterRequest;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceTest {

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private ItemRepository itemRepository;

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

        List<ShoppingListItemDTO> items = List.of(
                new ShoppingListItemDTO(1L, "Arroz", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN)
        );

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.findItemsByShoppingListId(10L))
                .thenReturn(items);

        ShoppingListDTO dto = shoppingListService.getActiveShoppingListByUser(user);

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

        when(shoppingListRepository.findById(20L))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListItemRepository.findItemsByShoppingListId(20L))
                .thenReturn(List.of());

        ShoppingListDTO dto = shoppingListService.getShoppingListWithItems(20L);

        assertEquals(20L, dto.shoppingListId());
    }

    @Test
    void shouldThrowExceptionWhenShoppingListNotFound() {
        when(shoppingListRepository.findById(99L))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.getShoppingListWithItems(99L)
        );

        assertEquals("SL1002", ex.getCode());
    }

    @Test
    void shouldCreateAndActivateShoppingList() {
        ShoppingList result = shoppingListService.createAndActiveShoppingList(user);

        assertTrue(result.isActive());
        assertEquals(user, result.getUser());

        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    void shouldRegisterItemInActiveShoppingList() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(1L);
        shoppingList.setActive(true);
        shoppingList.setUser(user);

        Item item = new Item();
        item.setItemId(5L);

        ShoppingListItemRegisterRequest request =
                new ShoppingListItemRegisterRequest(null, 5L, BigDecimal.TWO, BigDecimal.TEN);

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(itemRepository.findByUserAndItemId(user, 5L))
                .thenReturn(Optional.of(item));

        shoppingListService.registerShoppingListItem(request, user);

        verify(shoppingListItemRepository).save(any(ShoppingListItem.class));
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setActive(true);

        when(shoppingListRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(shoppingList));

        when(itemRepository.findByUserAndItemId(user, 99L))
                .thenReturn(Optional.empty());

        ShoppingListItemRegisterRequest request =
                new ShoppingListItemRegisterRequest(null, 99L, BigDecimal.ONE, BigDecimal.TEN);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.registerShoppingListItem(request, user)
        );

        assertEquals("SL1003", ex.getCode());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingItem() {
        when(shoppingListItemRepository.findById(1L))
                .thenReturn(Optional.empty());

        ShoppingListItemUpdateRequest request =
                new ShoppingListItemUpdateRequest(1L, BigDecimal.ONE, BigDecimal.TEN);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> shoppingListService.updateShoppingListItem(request)
        );

        assertEquals("SL1004", ex.getCode());
    }

    @Test
    void shouldRecalculateSubtotalWhenQuantityChanges() {
        ShoppingListItem item = spy(new ShoppingListItem());
        item.setPurchasedQuantity(BigDecimal.ONE);
        item.setUnitaryPrice(BigDecimal.TEN);

        when(shoppingListItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ShoppingListItemUpdateRequest request =
                new ShoppingListItemUpdateRequest(1L, BigDecimal.TWO, BigDecimal.TEN);

        shoppingListService.updateShoppingListItem(request);

        verify(item).recalculateSubtotal();
        verify(shoppingListItemRepository).save(item);
    }

    @Test
    void shouldDeleteShoppingListItem() {
        ShoppingListItem item = new ShoppingListItem();

        when(shoppingListItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        shoppingListService.deleteShoppingListItemById(1L);

        verify(shoppingListItemRepository).deleteById(1L);
    }


}
