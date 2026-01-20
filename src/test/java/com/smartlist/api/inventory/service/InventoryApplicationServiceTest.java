package com.smartlist.api.inventory.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryApplicationServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryApplicationService inventoryApplicationService;

    @Test
    void shouldProcessAllItemsWhenItemIsUpdated() {
        User user = new User();

        Item item1 = new Item();
        Item item2 = new Item();

        when(itemRepository.findByUser(user))
                .thenReturn(List.of(item1, item2));

        inventoryApplicationService.refreshInventory(user);

        verify(inventoryService, times(2))
                .processItem(any(Item.class));
    }

    @Test
    void shouldCallInventoryServiceWhenItemUpdated() {
        User user = new User();
        Item item = new Item();
        item.setUser(user);

        inventoryApplicationService.onItemUpdated(item);

        verify(inventoryService).processItem(item);
    }

    @Test
    void shouldAddStockSuccessfully() {
        User user = new User();
        Item item = new Item();
        item.setUser(user);
        item.setQuantity(new BigDecimal("5"));

        inventoryApplicationService.addStock(user, item, new BigDecimal("3"));

        assert item.getQuantity().compareTo(new BigDecimal("8")) == 0;
        assert item.getLastStockUpdate() != null;

        verify(itemRepository).save(item);
        verify(inventoryService).processItem(item);
    }

    @Test
    void shouldThrowExceptionWhenAddingStockToDifferentUser() {
        User user = new User();
        user.setUserId(1L);

        User anotherUser = new User();
        anotherUser.setUserId(2L);

        Item item = new Item();
        item.setUser(anotherUser);
        item.setQuantity(BigDecimal.ZERO);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> inventoryApplicationService.addStock(user, item, new BigDecimal("3"))
        );

        assertEquals("IA2002", exception.getCode());

        verify(itemRepository, never()).save(any());
        verify(inventoryService, never()).processItem(any());
    }

    @Test
    void shouldThrowExceptionWhenAddingNonPositiveQuantity() {
        User user = new User();
        Item item = new Item();
        item.setUser(user);

        BadRequestException exception = null;
        try {
            inventoryApplicationService.addStock(user, item, new BigDecimal("0"));
        } catch (BadRequestException ex) {
            exception = ex;
        }

        assert exception != null;
        assert exception.getCode().equals("IA2001");

        verify(itemRepository, never()).save(any());
        verify(inventoryService, never()).processItem(any());
    }
}
