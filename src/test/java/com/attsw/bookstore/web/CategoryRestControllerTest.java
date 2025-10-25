package com.attsw.bookstore.web;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryRestControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryRestController controller;

    @Test
    void shouldReturnAllCategories() {
        Category cat1 = new Category();
        cat1.setName("Fiction");
        Category cat2 = new Category();
        cat2.setName("Science");
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(cat1, cat2));

        List<Category> result = controller.all();

        assertEquals(2, result.size());
        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldCreateCategory() {
        Category input = new Category();
        input.setName("History");
        Category saved = new Category();
        saved.setId(1L);
        saved.setName("History");
        when(categoryService.saveCategory(input)).thenReturn(saved);

        Category result = controller.create(input);

        assertNotNull(result.getId());
        assertEquals("History", result.getName());
        verify(categoryService).saveCategory(input);
    }

    @Test
    void shouldReturnCategoryById() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        Category result = controller.one(1L);

        assertEquals(1L, result.getId());
        assertEquals("Fiction", result.getName());
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void shouldUpdateCategory() {
        Category input = new Category();
        input.setName("Updated");
        
        Category updated = new Category();
        updated.setId(1L);
        updated.setName("Updated");
        when(categoryService.saveCategory(any(Category.class))).thenReturn(updated);

        Category result = controller.update(1L, input);

        assertEquals(1L, result.getId());
        assertEquals("Updated", result.getName());
        assertEquals(1L, input.getId());
        verify(categoryService).saveCategory(any(Category.class));
    }

    @Test
    void shouldDeleteCategory() {
        doNothing().when(categoryService).deleteCategory(1L);

        controller.deleteCategory(1L);

        verify(categoryService).deleteCategory(1L);
    }
    
    @Test
    void shouldReturn404WhenCategoryNotFound() {
        when(categoryService.getCategoryById(999L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> controller.one(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("not found"));
        verify(categoryService).getCategoryById(999L);
    }
    
    
    
}
