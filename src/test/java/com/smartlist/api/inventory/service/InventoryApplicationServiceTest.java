package com.smartlist.api.inventory.service;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

}
