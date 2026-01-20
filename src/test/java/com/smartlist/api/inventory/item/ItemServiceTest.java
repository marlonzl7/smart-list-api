package com.smartlist.api.inventory.item;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequest;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequest;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.item.service.ItemService;
import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryApplicationService inventoryApplicationService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldRegisterItemWithoutCategory() {
        User user = new User();
        ItemRegisterRequest dto = ItemBuilder.anItem().withCategory(null).buildRegisterRequest();

        itemService.register(dto, user);

        verify(itemRepository).save(argThat(item ->
                "Item Teste".equals(item.getName())
                        && item.getUser().equals(user)
                        && item.getAvgConsumptionPerDay().compareTo(new BigDecimal("7")) == 0
                        && item.getCategory() == null
        ));
    }

    @Test
    void shouldRegisterItemWithCategory() {
        User user = new User();
        ItemRegisterRequest dto = ItemBuilder.anItem().withCategory(1L).buildRegisterRequest();
        Category category = ItemBuilder.anItem().withCategory(1L).buildCategory();

        when(categoryRepository.findByUserAndCategoryId(user, 1L))
                .thenReturn(Optional.of(category));

        itemService.register(dto, user);

        verify(itemRepository).save(argThat(item ->
                item.getCategory() == category
        ));
    }

    @Test
    void shouldThrowExceptionWhenItemAlreadyExists() {
        User user = new User();
        ItemRegisterRequest dto = ItemBuilder.anItem().buildRegisterRequest();

        when(itemRepository.existsByUserAndName(user, dto.name()))
                .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.register(dto, user));

        assertEquals("I1013", exception.getCode());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionForNegativeValues() {
        User user = new User();
        ItemRegisterRequest dto = ItemBuilder.anItem()
                .withQuantity(new BigDecimal("-1"))
                .buildRegisterRequest();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.register(dto, user));
        assertEquals("I1006", ex.getCode());
    }

    @Test
    void shouldThrowExceptionForIncompleteAvgConsumption() {
        User user = new User();
        ItemRegisterRequest dto = ItemBuilder.anItem()
                .withAvgConsumption(new BigDecimal("7"), null)
                .buildRegisterRequest();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.register(dto, user));
        assertEquals("I1012", ex.getCode());
    }

    @Test
    void shouldUpdateItemSuccessfully() {
        User user = new User();
        Item item = ItemBuilder.anItem().buildItem(user);
        ItemUpdateRequest dto = ItemBuilder.anItem()
                .withName("Novo Item")
                .withQuantity(new BigDecimal("5"))
                .withPrice(new BigDecimal("20.00"))
                .withCategory(2L)
                .withCriticalOverride(3)
                .buildUpdateRequest();
        Category category = ItemBuilder.anItem().withCategory(2L).buildCategory();

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));
        when(categoryRepository.findByUserAndCategoryId(user, 2L))
                .thenReturn(Optional.of(category));

        itemService.update(1L, dto, user);

        verify(itemRepository).save(argThat(saved ->
                "Novo Item".equals(saved.getName())
                        && saved.getQuantity().compareTo(new BigDecimal("5")) == 0
                        && saved.getPrice().compareTo(new BigDecimal("20.00")) == 0
                        && saved.getCategory() == category
                        && saved.getCriticalQuantityDaysOverride() == 3
        ));

        verify(inventoryApplicationService).onItemUpdated(item);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingItemWithNonExistentCategory() {
        User user = new User();
        Item item = ItemBuilder.anItem().buildItem(user);
        ItemUpdateRequest dto = ItemBuilder.anItem().withCategory(99L).buildUpdateRequest();

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));
        when(categoryRepository.findByUserAndCategoryId(user, 99L))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.update(1L, dto, user));
        assertEquals("I1005", ex.getCode());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem() {
        User user = new User();
        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> itemService.deleteById(1L, user));
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        User user = new User();
        Item item = ItemBuilder.anItem().buildItem(user);

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));

        itemService.deleteById(1L, user);

        verify(itemRepository).delete(item);
    }
}
