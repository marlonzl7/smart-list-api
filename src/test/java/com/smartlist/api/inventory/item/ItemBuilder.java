package com.smartlist.api.inventory.item;

import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequest;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequest;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.user.model.User;

import java.math.BigDecimal;

public class ItemBuilder {

    private Long categoryId = null;
    private String name = "Item Teste";
    private BigDecimal quantity = new BigDecimal("1");
    private UnitOfMeasure unit = UnitOfMeasure.UNIT;
    private BigDecimal avgConsumptionValue = new BigDecimal("7");
    private AverageConsumptionUnit avgConsumptionUnit = AverageConsumptionUnit.DAY;
    private BigDecimal price = new BigDecimal("10.00");
    private Integer criticalQuantityDaysOverride = null;

    public static ItemBuilder anItem() {
        return new ItemBuilder();
    }

    public ItemBuilder withCategory(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder withQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public ItemBuilder withUnit(UnitOfMeasure unit) {
        this.unit = unit;
        return this;
    }

    public ItemBuilder withAvgConsumption(BigDecimal value, AverageConsumptionUnit unit) {
        this.avgConsumptionValue = value;
        this.avgConsumptionUnit = unit;
        return this;
    }

    public ItemBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ItemBuilder withCriticalOverride(Integer days) {
        this.criticalQuantityDaysOverride = days;
        return this;
    }

    public ItemRegisterRequest buildRegisterRequest() {
        return new ItemRegisterRequest(
                categoryId,
                name,
                quantity,
                unit,
                avgConsumptionValue,
                avgConsumptionUnit,
                price,
                criticalQuantityDaysOverride
        );
    }

    public ItemUpdateRequest buildUpdateRequest() {
        return new ItemUpdateRequest(
                name,
                quantity,
                unit,
                price,
                avgConsumptionValue,
                avgConsumptionUnit,
                criticalQuantityDaysOverride,
                categoryId
        );
    }

    public Item buildItem(User user) {
        Item item = new Item();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setAvgConsumptionValue(avgConsumptionValue);
        item.setAvgConsumptionUnit(avgConsumptionUnit);
        item.setPrice(price);
        item.setUser(user);
        item.setCriticalQuantityDaysOverride(criticalQuantityDaysOverride);
        return item;
    }

    public Category buildCategory() {
        if (categoryId == null) {
            categoryId = 1L;
        }
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Categoria Teste");
        return category;
    }
}
