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
        long days = ChronoUnit.DAYS.between(item.getLastStockUpdate(), LocalDate.now());
        return item.getQuantity().subtract(
                item.getAvgConsumptionPerDay().multiply(
                        BigDecimal.valueOf(days)
                )
        );
    }

    public boolean isCritical(Item item, BigDecimal virtualStock) {
        long criticalDays = item.getCriticalQuantityDaysOverride() != null ? item.getCriticalQuantityDaysOverride() : item.getUser().getCriticalQuantityDays();
        BigDecimal criticalLimit = item.getAvgConsumptionPerDay().multiply(BigDecimal.valueOf(criticalDays));

        return virtualStock.compareTo(criticalLimit) <= 0;
    }

    public void processItem(Item item) {
        log.info("Iniciado processamento do Item ID: {}", item.getItemId());
        BigDecimal virtualStock = calculateVirtualStock(item);

        if (isCritical(item, virtualStock)) {
            shoppingListService.addItemToShoppingList(item, item.getUser());
        }

        log.info("Processamento finalizado do Item ID: {}", item.getItemId());
    }
}
