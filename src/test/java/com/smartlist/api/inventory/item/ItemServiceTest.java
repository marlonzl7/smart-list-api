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
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemService itemService;

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
                new BigDecimal("10.50")
        );

        itemService.register(dto, user);

        verify(itemRepository).save(argThat(item ->
                item.getName().equals("Arroz") &&
                        item.getUser().equals(user) &&
                        item.getAvgConsumptionPerDay().compareTo(new BigDecimal("1.000000")) == 0
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
                new BigDecimal("8.00")
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
                1L,
                "Novo nome",
                new BigDecimal("1"),
                UnitOfMeasure.UNIT,
                new BigDecimal("5"),
                new BigDecimal("1"),
                AverageConsumptionUnit.DAY,
                1L
        );

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                itemService.update(dto, user)
        );
    }

    @Test
    void shouldRecalculateAvgConsumptionWhenUnitChanges() {
        User user = new User();

        Item item = new Item();
        item.setAvgConsumptionValue(new BigDecimal("30"));
        item.setAvgConsumptionUnit(AverageConsumptionUnit.MONTH);

        when(itemRepository.findByUserAndItemId(user, 1L))
                .thenReturn(Optional.of(item));

        ItemUpdateRequestDTO dto = new ItemUpdateRequestDTO(
                1L,
                "Item",
                new BigDecimal("1"),
                UnitOfMeasure.UNIT,
                new BigDecimal("10"),
                new BigDecimal("7"),
                AverageConsumptionUnit.WEEK,
                1L
        );

        itemService.update(dto, user);

        verify(itemRepository).save(argThat(saved ->
                saved.getAvgConsumptionPerDay()
                        .compareTo(new BigDecimal("1.000000")) == 0
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
