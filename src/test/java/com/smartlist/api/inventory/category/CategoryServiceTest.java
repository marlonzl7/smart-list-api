package com.smartlist.api.inventory.category;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.dto.CategoryListResponseDTO;
import com.smartlist.api.inventory.category.dto.CategoryRegisterRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryUpdateRequestDTO;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.category.service.CategoryService;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    /*
     * TESTES NECESSÁRIOS:
     * CENÁRIO FELIZ
     * CENÁRIO DE ERRO
     * REGRAS DE NEGÓCIO IMPORTANTES
     */

    @Test
    void shouldListCategoriesByUserWithPagination() {
        // Arrange
        User user = new User();
        Pageable pageable = Pageable.ofSize(10);

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Mercado");

        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findByUser(user, pageable))
                .thenReturn(page);

        // Act
        Page<CategoryListResponseDTO> result = categoryService.list(user, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Mercado", result.getContent().get(0).name());

        verify(categoryRepository).findByUser(user, pageable);
    }

    @Test
    void shouldListAllCategoriesByUser() {
        // Arrange
        User user = new User();

        Category category1 = new Category();
        category1.setCategoryId(1L);
        category1.setName("Mercado");

        Category category2 = new Category();
        category2.setCategoryId(2L);
        category2.setName("Farmácia");

        when(categoryRepository.findByUser(user))
                .thenReturn(List.of(category1, category2));

        // Act
        List<CategoryListResponseDTO> result =
                categoryService.listAll(user);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Mercado", result.get(0).name());
        assertEquals("Farmácia", result.get(1).name());

        verify(categoryRepository).findByUser(user);
    }

    @Test
    void shouldRegisterCategorySuccessfully() {
        // Arrange
        User user = new User();
        CategoryRegisterRequestDTO dto =
                new CategoryRegisterRequestDTO("Mercado");

        when(categoryRepository.findByUserAndName(user, "Mercado"))
                .thenReturn(Optional.empty());

        // Act
        categoryService.register(dto, user);

        // Assert
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        // Arrange
        User user = new User();
        CategoryRegisterRequestDTO dto =
                new CategoryRegisterRequestDTO("Mercado");

        when(categoryRepository.findByUserAndName(user, "Mercado"))
                .thenReturn(Optional.of(new Category()));

        // Act + Assert
        BadRequestException exception =
                assertThrows(BadRequestException.class,
                        () -> categoryService.register(dto, user));

        assertEquals("C1001", exception.getCode());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCategory() {
        // Arrange
        User user = new User();
        CategoryUpdateRequestDTO dto =
                new CategoryUpdateRequestDTO(1L, "Nova Categoria");

        when(categoryRepository.findByUserAndCategoryId(user, 1L))
                .thenReturn(Optional.empty());

        // Act + Assert
        BadRequestException exception =
                assertThrows(BadRequestException.class,
                        () -> categoryService.update(dto, user));

        assertEquals("C1003", exception.getCode());
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        // Arrange
        User user = new User();
        Category category = new Category();

        when(categoryRepository.findByUserAndCategoryId(user, 1L))
                .thenReturn(Optional.of(category));

        // Act
        categoryService.deleteById(1L, user);

        // Assert
        verify(categoryRepository).deleteById(1L);
    }

}
