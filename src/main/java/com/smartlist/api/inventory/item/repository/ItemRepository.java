package com.smartlist.api.inventory.item.repository;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByUserAndItemId(User user, Long itemId);
    Page<Item> findByUser(User user, Pageable pageable);
    List<Item> findByUser(User user);
}
