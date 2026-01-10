package com.smartlist.api.inventory;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private ShoppingListService shoppingListService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldCalculateVirtualStockBasedOnDaysAndAvgConsumption() {
        Item item = new Item();
        item.setQuantity(new BigDecimal("10"));
        item.setAvgConsumptionPerDay(new BigDecimal("1"));
        item.setLastStockUpdate(LocalDate.now().minusDays(3));

        BigDecimal result = inventoryService.calculateVirtualStock(item);

        assertEquals(new BigDecimal("7"), result);
    }

    @Test
    void shouldReturnTrueWhenVirtualStockIsBelowCriticalLimit() {
        Item item = new Item();
        User user = new User();

        user.setCriticalQuantityDays(5);
        item.setUser(user);
        item.setAvgConsumptionPerDay(new BigDecimal("1"));

        BigDecimal virtualStock = new BigDecimal("5");

        boolean result = inventoryService.isCritical(item, virtualStock);

        assertTrue(result);
    }

    @Test
    void shouldUseItemCriticalOverrideWhenPresent() {
        Item item = new Item();
        User user = new User();

        user.setCriticalQuantityDays(10);
        item.setUser(user);
        item.setCriticalQuantityDaysOverride(3);
        item.setAvgConsumptionPerDay(new BigDecimal("1"));

        BigDecimal virtualStock = new BigDecimal("3");

        boolean result = inventoryService.isCritical(item, virtualStock);

        assertTrue(result);
    }

    @Test
    void shouldAddItemToShoppingListWhenItemIsCritical() {
        User user = new User();
        user.setCriticalQuantityDays(5);

        Item item = new Item();
        item.setUser(user);
        item.setQuantity(new BigDecimal("2"));
        item.setAvgConsumptionPerDay(new BigDecimal("1"));
        item.setLastStockUpdate(LocalDate.now().minusDays(2));

        inventoryService.processItem(item);

        verify(shoppingListService)
                .addItemToShoppingList(item, user);
    }

    @Test
    void shouldNotAddItemWhenNotCritical() {
        Item item = new Item();
        User user = new User();

        user.setCriticalQuantityDays(5);
        item.setUser(user);
        item.setQuantity(new BigDecimal("10"));
        item.setAvgConsumptionPerDay(new BigDecimal("1"));
        item.setLastStockUpdate(LocalDate.now().minusDays(1));

        inventoryService.processItem(item);

        verify(shoppingListService, never())
                .addItemToShoppingList(any(), any());
    }

}
