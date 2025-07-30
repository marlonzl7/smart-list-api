package com.smartlist.api.inventory.category.repository;

import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUserAndCategoryId(User user, Long categoryId);
    Optional<Category> findByUserAndName(User user, String name);
    Page<Category> findByUser(User user, Pageable pageable);
    List<Category> findByUser(User user);
}
