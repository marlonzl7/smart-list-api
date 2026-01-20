package com.smartlist.api.shoppinglistitem.model;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_list_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_list_item_id")
    private Long shoppingListItemId;

    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shopping_list"))
    private ShoppingList shoppingList;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item"))
    private Item item;

    @Column(name = "purchased_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal purchasedQuantity;

    @Column(name = "unitary_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitaryPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ShoppingListItem create(Item item, ShoppingList shoppingList) {
        ShoppingListItem sli = new ShoppingListItem();
        sli.setItem(item);
        sli.setShoppingList(shoppingList);
        sli.setPurchasedQuantity(BigDecimal.ZERO);
        sli.setUnitaryPrice(null);
        sli.recalculateSubtotal();
        return sli;
    }

    public void recalculateSubtotal() {
        if (unitaryPrice != null && purchasedQuantity != null) {
            this.subtotal = unitaryPrice.multiply(purchasedQuantity);
        }
    }
}
