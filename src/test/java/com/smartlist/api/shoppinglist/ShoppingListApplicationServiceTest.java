package com.smartlist.api.shoppinglist;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.item.service.ItemService;
import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.shoppinglist.dto.FinalizePurchaseRequest;
import com.smartlist.api.shoppinglist.dto.PurchasedItemDTO;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglist.repository.ShoppingListRepository;
import com.smartlist.api.shoppinglist.service.ShoppingListApplicationService;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
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
class ShoppingListApplicationServiceTest {

    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private InventoryApplicationService inventoryApplicationService;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ShoppingListApplicationService applicationService;

    private User user;
    private ShoppingList shoppingList;
    private ShoppingListItem item1;
    private ShoppingListItem item2;
    private Item inventoryItem1;
    private Item inventoryItem2;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);

        inventoryItem1 = new Item();
        inventoryItem1.setItemId(100L);

        inventoryItem2 = new Item();
        inventoryItem2.setItemId(200L);

        shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(10L);
        shoppingList.setUser(user);
        shoppingList.setActive(true);

        item1 = new ShoppingListItem();
        item1.setShoppingListItemId(1L);
        item1.setItem(inventoryItem1);
        item1.setShoppingList(shoppingList);

        item2 = new ShoppingListItem();
        item2.setShoppingListItemId(2L);
        item2.setItem(inventoryItem2);
        item2.setShoppingList(shoppingList);

        shoppingList.setItems(List.of(item1, item2));

        when(
                shoppingListRepository.findByShoppingListIdAndUserAndActiveTrue(
                        shoppingList.getShoppingListId(),
                        user
                )
        ).thenReturn(Optional.of(shoppingList));
    }

    @Test
    void shouldFinalizeShoppingListSuccessfully() {
        FinalizePurchaseRequest request = new FinalizePurchaseRequest(
                List.of(
                        new PurchasedItemDTO(1L, new BigDecimal("2"), new BigDecimal("5.00")),
                        new PurchasedItemDTO(2L, new BigDecimal("1"), new BigDecimal("10.00"))
                )
        );

        boolean result = applicationService.finalizeShoppingList(
                shoppingList.getShoppingListId(),
                request,
                user
        );

        assertTrue(result);
        assertFalse(shoppingList.isActive());

        assertEquals(new BigDecimal("2"), item1.getPurchasedQuantity());
        assertEquals(new BigDecimal("5.00"), item1.getUnitaryPrice());
        assertEquals(new BigDecimal("10.00"), item1.getSubtotal());

        assertEquals(new BigDecimal("1"), item2.getPurchasedQuantity());
        assertEquals(new BigDecimal("10.00"), item2.getUnitaryPrice());
        assertEquals(new BigDecimal("10.00"), item2.getSubtotal());

        verify(inventoryApplicationService)
                .addStock(user, inventoryItem1, new BigDecimal("2"));

        verify(inventoryApplicationService)
                .addStock(user, inventoryItem2, new BigDecimal("1"));

        verify(shoppingListRepository).save(shoppingList);
    }

    @Test
    void shouldThrowExceptionWhenPurchasedQuantityIsNegative() {
        FinalizePurchaseRequest request = new FinalizePurchaseRequest(
                List.of(
                        new PurchasedItemDTO(1L, new BigDecimal("-1"), new BigDecimal("5.00"))
                )
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> applicationService.finalizeShoppingList(
                        shoppingList.getShoppingListId(),
                        request,
                        user
                )
        );

        assertEquals("SLA1002", ex.getCode());
    }

    @Test
    void shouldThrowExceptionWhenUnitaryPriceIsNull() {
        FinalizePurchaseRequest request = new FinalizePurchaseRequest(
                List.of(
                        new PurchasedItemDTO(1L, new BigDecimal("2"), null)
                )
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> applicationService.finalizeShoppingList(
                        shoppingList.getShoppingListId(),
                        request,
                        user
                )
        );

        assertEquals("SLA1003", ex.getCode());
    }

    @Test
    void shouldResetItemValuesWhenItemIsNotPresentInRequest() {
        FinalizePurchaseRequest request = new FinalizePurchaseRequest(
                List.of(
                        new PurchasedItemDTO(1L, new BigDecimal("2"), new BigDecimal("5.00"))
                )
        );

        applicationService.finalizeShoppingList(
                shoppingList.getShoppingListId(),
                request,
                user
        );

        assertEquals(BigDecimal.ZERO, item2.getPurchasedQuantity());
        assertNull(item2.getUnitaryPrice());
        assertEquals(BigDecimal.ZERO, item2.getSubtotal());

        verify(inventoryApplicationService, never())
                .addStock(eq(user), eq(inventoryItem2), any());
    }

    @Test
    void shouldThrowExceptionWhenShoppingListDoesNotExist() {
        when(
                shoppingListRepository.findByShoppingListIdAndUserAndActiveTrue(
                        shoppingList.getShoppingListId(),
                        user
                )
        ).thenReturn(Optional.empty());

        FinalizePurchaseRequest request = new FinalizePurchaseRequest(List.of());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> applicationService.finalizeShoppingList(
                        shoppingList.getShoppingListId(),
                        request,
                        user
                )
        );

        assertEquals("SLA1001", ex.getCode());
    }
}
