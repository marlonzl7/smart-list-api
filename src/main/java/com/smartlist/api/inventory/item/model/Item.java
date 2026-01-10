package com.smartlist.api.inventory.item.model;

import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_category"))
    private Category category;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unit;

    @Column(name = "avg_consumption_value", nullable = false, precision = 10, scale = 3)
    private BigDecimal avgConsumptionValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "avg_consumption_unit")
    private AverageConsumptionUnit avgConsumptionUnit;

    @Column(name = "avg_consumption_per_day", nullable = false, precision = 10, scale = 3)
    private BigDecimal avgConsumptionPerDay;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "in_shopping_list", nullable = false)
    private boolean inShoppingList = false;

    @Column(name = "last_stock_update")
    private LocalDate lastStockUpdate;

    @Column(name = "critical_quantity_days_override")
    private Integer criticalQuantityDaysOverride;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
