package com.smartlist.api.shoppinglist.repository;

import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    Optional<ShoppingList> findByUserAndActiveTrue(User user);
    Optional<ShoppingList> findByShoppingListIdAndUserAndActiveTrue(Long shoppingListId, User user);
}
