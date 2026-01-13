package com.smartlist.api.inventory.item.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.item.dto.ItemListResponseDTO;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequestDTO;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequestDTO;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.inventory.service.InventoryService;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryApplicationService inventoryApplicationService;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository, InventoryApplicationService inventoryApplicationService) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    public Page<ItemListResponseDTO> list(User user, Pageable pageable) {
        return itemRepository.findByUser(user, pageable)
                .map(item -> new ItemListResponseDTO(
                        item.getItemId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getUnit().getLabel(),
                        item.getPrice(),
                        item.getAvgConsumptionValue(),
                        item.getAvgConsumptionUnit().name(),
                        item.getCategory().getCategoryId(),
                        item.getCategory().getName()
                ));
    }

    public void register(ItemRegisterRequestDTO dto, User user) {
        log.info("Iniciando tentativa de cadastro de item");

        Item item = new Item();
        item.setUser(user);
        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setPrice(dto.price());
        item.setUnit(dto.unit());
        item.setAvgConsumptionValue(dto.avgConsumptionValue());
        item.setAvgConsumptionUnit(dto.avgConsumptionUnit());
        item.setLastStockUpdate(LocalDate.now());

        if (dto.criticalQuantityDaysOverride() != null) {
            item.setCriticalQuantityDaysOverride(dto.criticalQuantityDaysOverride());
        }

        item.setAvgConsumptionPerDay(convertToDailyAverage(dto.avgConsumptionValue(), item.getAvgConsumptionUnit()));

        if (dto.categoryId() != null) {

            Category category = categoryRepository.findByUserAndCategoryId(user, dto.categoryId())
                    .orElseThrow(() -> {
                        log.error("Tentativa de cadastro de item com categoria inexistente");
                        return new BadRequestException("I1003", "Categoria inexistente");
                    });

            item.setCategory(category);
        }

        itemRepository.save(item);

        log.info("Cadastro de item realizado com sucesso.");
    }

    public void update(Long itemId, ItemUpdateRequestDTO dto, User user) {
        log.info("Iniciando tentativa de atualização de Item");

        Item item = itemRepository
                .findByUserAndItemId(user, itemId)
                .orElseThrow(() -> {
                    log.error("Tentativa de atualização de item inexistente");
                    return new BadRequestException("I1004", "Item inexistente");
                });

        if (!dto.name().equals(item.getName())) {
            item.setName(dto.name());
        }

        if (!dto.quantity().equals(item.getQuantity())) {
            item.setQuantity(dto.quantity());
            item.setLastStockUpdate(LocalDate.now());
        }

        if (!dto.unit().equals(item.getUnit())) {
            item.setUnit(dto.unit());
        }

        if (!dto.price().equals(item.getPrice())) {
            item.setPrice(dto.price());
        }

        if (!dto.avgConsumptionValue().equals(item.getAvgConsumptionValue())) {
            item.setAvgConsumptionValue(dto.avgConsumptionValue());
        }

        boolean shouldRecalculateAvgPerDay = false;

        if (!dto.avgConsumptionValue().equals(item.getAvgConsumptionValue())) {
            item.setAvgConsumptionValue(dto.avgConsumptionValue());
            shouldRecalculateAvgPerDay = true;
        }

        if (!dto.avgConsumptionUnit().equals(item.getAvgConsumptionUnit())) {
            shouldRecalculateAvgPerDay = true;
        }

        if (shouldRecalculateAvgPerDay) {
            item.setAvgConsumptionPerDay(convertToDailyAverage(item.getAvgConsumptionValue(), item.getAvgConsumptionUnit()));
        }

        Integer override = dto.criticalQuantityDaysOverride();
        if (!Objects.equals(override, item.getCriticalQuantityDaysOverride())) {
            item.setCriticalQuantityDaysOverride(override);
        }

        itemRepository.save(item);
        inventoryApplicationService.onItemUpdated(user);

        log.info("Item atualizado com sucesso.");
    }

    public void deleteById(Long itemId, User user) {
        log.info("Iniciando tentativa de exclusão de item");

        Item item = itemRepository.findByUserAndItemId(user, itemId).orElseThrow(() -> {
            log.error("Tentativa de exclusão de item inexistente");
            return new BadRequestException("I1004", "Item inexistente");
        });

        itemRepository.deleteById(itemId);

        log.info("Item excluído com sucesso.");
    }

    private BigDecimal convertToDailyAverage(BigDecimal value, AverageConsumptionUnit unit) {
        return switch (unit) {
            case MONTH -> value.divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
            case WEEK -> value.divide(new BigDecimal("7"), 6, RoundingMode.HALF_UP);
            case DAY -> value;
        };
    }
}
