package com.smartlist.api.shoppinglist;

import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.service.ShoppingListApplicationService;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingListApplicationServiceTest {
    
    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private InventoryApplicationService inventoryApplicationService;

    @InjectMocks
    private ShoppingListApplicationService shoppingListApplicationService;

    @Test
    void shouldGetActiveShoppingListAndProcessInventory() {
        User user = new User();
        ShoppingListDTO dto = mock(ShoppingListDTO.class);

        when(shoppingListService.getActiveShoppingListByUser(user))
                .thenReturn(dto);

        ShoppingListDTO result =
                shoppingListApplicationService.getActiveShoppingList(user);

        assertEquals(dto, result);

        verify(inventoryApplicationService)
                .onItemUpdated(user);
    }
}
