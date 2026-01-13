package com.smartlist.api.inventory.item;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequestDTO;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequestDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemService itemService;

    @Mock
    private InventoryApplicationService inventoryApplicationService;

    @Test
    void shouldRegisterItemWithoutCategory() {
        User user = new User();

        ItemRegisterRequestDTO dto = new ItemRegisterRequestDTO(
                null,
                "Arroz",
                new BigDecimal("2"),
                UnitOfMeasure.KG,
                new BigDecimal("30"),
                AverageConsumptionUnit.MONTH,
                new BigDecimal("10.50"),
                null
        );

        itemService.register(dto, user);

        verify(itemRepository).save(argThat(item ->
                "Arroz".equals(item.getName())
                        && item.getUser().equals(user)
                        && item.getAvgConsumptionPerDay()
                        .compareTo(new BigDecimal("1.000000")) == 0
        ));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        User user = new User();

        ItemRegisterRequestDTO dto = new ItemRegisterRequestDTO(
                99L,
                "Feijão",
                new BigDecimal("1"),
                UnitOfMeasure.KG,
                new BigDecimal("7"),
                AverageConsumptionUnit.WEEK,
                new BigDecimal("8.00"),
                null
        );

        when(categoryRepository.findByUserAndCategoryId(user, 99L))
                .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> itemService.register(dto, user)
        );

        assertEquals("I1003", exception.getCode());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentItem() {
        User user = new User();

        ItemUpdateRequestDTO dto = new ItemUpdateRequestDTO(
                "Novo nome",
                new BigDecimal("1"),
                UnitOfMeasure.UNIT,
                new BigDecimal("5.00"),
                new BigDecimal("1"),
                AverageConsumptionUnit.DAY,
                null,
                1L
        );

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                itemService.update(1L, dto, user)
        );
    }

    @Test
    void shouldRecalculateAvgConsumptionWhenConsumptionParametersChange() {
        User user = new User();

        Item item = new Item();
        item.setName("Item");
        item.setUser(user);
        item.setQuantity(new BigDecimal("10"));
        item.setUnit(UnitOfMeasure.UNIT);

        item.setAvgConsumptionValue(new BigDecimal("30"));
        item.setAvgConsumptionUnit(AverageConsumptionUnit.MONTH);
        item.setAvgConsumptionPerDay(new BigDecimal("1.000000"));

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));

        ItemUpdateRequestDTO dto = new ItemUpdateRequestDTO(
                "Item",
                new BigDecimal("10"),
                UnitOfMeasure.UNIT,
                new BigDecimal("7.00"),
                new BigDecimal("7"),
                AverageConsumptionUnit.WEEK,
                null,
                1L
        );

        itemService.update(1L, dto, user);

        verify(itemRepository).save(argThat(saved ->
                saved.getAvgConsumptionPerDay()
                        .subtract(new BigDecimal("0.233333"))
                        .abs()
                        .compareTo(new BigDecimal("0.000001")) < 0
        ));
    }


    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem() {
        User user = new User();

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                itemService.deleteById(1L, user)
        );
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        User user = new User();
        Item item = new Item();

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));

        itemService.deleteById(1L, user);

        verify(itemRepository).deleteById(1L);
    }
}
