package com.smartlist.api.inventory.item.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.item.dto.ItemListResponse;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequest;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequest;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.inventory.service.InventoryApplicationService;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

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

    public Page<ItemListResponse> list(User user, Pageable pageable) {
        return itemRepository.findByUser(user, pageable)
                .map(item -> new ItemListResponse(
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

    public void register(ItemRegisterRequest dto, User user) {
        log.info("Cadastro de item iniciado. UserId={}, Name={}", user.getUserId(), dto.name());

        if (itemRepository.existsByUserAndName(user, dto.name())) {
            log.warn(
                    "Tentativa de cadastro de item duplicado. UserId={}, ItemName={}",
                    user.getUserId(),
                    dto.name()
            );
            throw new BadRequestException("I1013", "Item já cadastrado");
        }

        validateNonNegative(dto.quantity(), "I1006", "Quantidade não pode ser negativa");
        validateNonNegative(dto.price(), "I1007", "Preço não pode ser negativo");
        validateNonNegative(dto.avgConsumptionValue(), "I1008", "Consumo médio não pode ser negativo");

        validateConsumptionUnit(dto.avgConsumptionValue(), dto.avgConsumptionUnit());

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
                        log.warn(
                                "Tentativa de cadastro de item com categoria inexistente. UserId={}, CategoryId={}",
                                user.getUserId(),
                                dto.categoryId()
                        );
                        return new BadRequestException("I1003", "Categoria inexistente");
                    });

            item.setCategory(category);
        }

        itemRepository.save(item);

        log.info(
                "Item cadastrado com sucesso. UserId={}, ItemId={}",
                user.getUserId(),
                item.getItemId()
        );
    }

    public void update(Long itemId, ItemUpdateRequest dto, User user) {
        log.info("Atualização de item iniciada. UserId={}, ItemId={}", user.getUserId(), itemId);

        Item item = itemRepository.findByUserAndItemId(user, itemId)
                .orElseThrow(() -> {
                    log.warn(
                            "Tentativa de atualização de item inexistente. UserId={}, ItemId={}",
                            user.getUserId(),
                            itemId
                    );
                    return new BadRequestException("I1004", "Item inexistente");
                });

        validateNonNegative(dto.quantity(), "I1009", "Quantidade não pode ser negativa");
        validateNonNegative(dto.price(), "I1010", "Preço não pode ser negativo");
        validateNonNegative(dto.avgConsumptionValue(), "I1011", "Consumo médio não pode ser negativo");

        validateConsumptionUnit(dto.avgConsumptionValue(), dto.avgConsumptionUnit());

        if (dto.name() != null && !dto.name().equals(item.getName())) {
            item.setName(dto.name());
        }

        if (dto.quantity() != null && !dto.quantity().equals(item.getQuantity())) {
            item.setQuantity(dto.quantity());
            item.setLastStockUpdate(LocalDate.now());
        }

        if (dto.unit() != null && !dto.unit().equals(item.getUnit())) {
            item.setUnit(dto.unit());
        }

        if (dto.price() != null && !dto.price().equals(item.getPrice())) {
            item.setPrice(dto.price());
        }

        boolean shouldRecalculateAvgPerDay = false;

        if (dto.avgConsumptionValue() != null && !dto.avgConsumptionValue().equals(item.getAvgConsumptionValue())) {
            item.setAvgConsumptionValue(dto.avgConsumptionValue());
            shouldRecalculateAvgPerDay = true;
        }

        if (dto.avgConsumptionUnit() != null && !dto.avgConsumptionUnit().equals(item.getAvgConsumptionUnit())) {
            item.setAvgConsumptionUnit(dto.avgConsumptionUnit());
            shouldRecalculateAvgPerDay = true;
        }

        if (shouldRecalculateAvgPerDay) {
            item.setAvgConsumptionPerDay(convertToDailyAverage(item.getAvgConsumptionValue(), item.getAvgConsumptionUnit()));
        }

        Integer override = dto.criticalQuantityDaysOverride();
        if (!Objects.equals(override, item.getCriticalQuantityDaysOverride())) {
            item.setCriticalQuantityDaysOverride(override);
        }

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findByUserAndCategoryId(user, dto.categoryId())
                    .orElseThrow(() -> {
                        throw new BadRequestException("I1005", "Categoria inexistente");
                    });

            item.setCategory(category);
        } else {
            item.setCategory(null);
        }

        itemRepository.save(item);
        inventoryApplicationService.onItemUpdated(item);

        log.info("Item atualizado com sucesso. UserId={}, ItemId={}", user.getUserId(), itemId);
    }

    public void deleteById(Long itemId, User user) {
        log.info(
                "Tentativa de exclusão de item iniciada. UserId={}, ItemId={}",
                user.getUserId(),
                itemId
        );

        Item item = itemRepository.findByUserAndItemId(user, itemId)
                .orElseThrow(() -> {
                    log.info(
                            "Tentativa de exclusão de item iniciada. UserId={}, ItemId={}",
                            user.getUserId(),
                            itemId
                    );
                    throw new BadRequestException("I1004", "Item inexistente");
                });

        itemRepository.delete(item);

        log.info(
                "Tentativa de exclusão de item iniciada. UserId={}, ItemId={}",
                user.getUserId(),
                itemId
        );
    }

    private void validateNonNegative(BigDecimal value, String erroCode, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(erroCode, message);
        }
    }

    private void validateConsumptionUnit(BigDecimal consumptionValue, AverageConsumptionUnit unit) {
        boolean onlyOneFilled =
                (consumptionValue == null && unit != null) ||
                (consumptionValue != null && unit == null);

        if (onlyOneFilled) {
            throw new BadRequestException("I1012", "Consumo médio e unidade devem ser informados juntos");
        }
    }

    private BigDecimal convertToDailyAverage(BigDecimal value, AverageConsumptionUnit unit) {
        return switch (unit) {
            case MONTH -> value.divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
            case WEEK -> value.divide(new BigDecimal("7"), 6, RoundingMode.HALF_UP);
            case DAY -> value;
        };
    }
}
