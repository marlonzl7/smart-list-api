package com.smartlist.api.inventory.service;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class InventoryService {

    private final ShoppingListService shoppingListService;

    public InventoryService(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    public BigDecimal calculateVirtualStock(Item item) {
        if (item.getLastStockUpdate() == null) {
            log.debug(
                    "Item sem lastStockUpdate. Assumindo estoque atual como base. ItemId={}",
                    item.getItemId()
            );
            return item.getQuantity();
        }

        long days = ChronoUnit.DAYS.between(item.getLastStockUpdate(), LocalDate.now());
        days = Math.max(days, 0);

        BigDecimal virtualStock = item.getQuantity().subtract(
                item.getAvgConsumptionPerDay().multiply(BigDecimal.valueOf(days))
        );

        return virtualStock.max(BigDecimal.ZERO);
    }

    private long resolveCriticalDays(Item item) {
        return item.getCriticalQuantityDaysOverride() != null
                ? item.getCriticalQuantityDaysOverride()
                : item.getUser().getCriticalQuantityDays();
    }

    public boolean isCritical(Item item, BigDecimal virtualStock) {
        long criticalDays = resolveCriticalDays(item);
        BigDecimal criticalLimit = item.getAvgConsumptionPerDay().multiply(BigDecimal.valueOf(criticalDays));

        return virtualStock.compareTo(criticalLimit) <= 0;
    }

    public void processItem(Item item) {
        log.debug("Processando item. ItemId={}", item.getItemId());
        BigDecimal virtualStock = calculateVirtualStock(item);

        if (isCritical(item, virtualStock)) {
            shoppingListService.addItemToShoppingList(item, item.getUser());
        }
    }
}
