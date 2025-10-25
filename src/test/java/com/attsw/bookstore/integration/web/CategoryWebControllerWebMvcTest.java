package com.attsw.bookstore.integration.web;

import com.attsw.bookstore.web.CategoryWebController;
import com.attsw.bookstore.service.CategoryService;
import com.attsw.bookstore.service.BookService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.attsw.bookstore.model.Category;

@WebMvcTest(CategoryWebController.class)
class CategoryWebControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CategoryService categoryService;
    
    @MockitoBean
    private BookService bookService;

    @Test
    void shouldShowCategoryListPage() throws Exception {
        Category c = new Category();
        c.setName("Software");
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(c));

        mvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/list"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void shouldShowAddCategoryForm() throws Exception {
        mvc.perform(get("/categories/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/new"))
            .andExpect(model().attributeExists("category"));
    }

    @Test
    void shouldSaveCategoryAndRedirectToList() throws Exception {
        Category saved = new Category();
        saved.setId(1L);
        saved.setName("Fiction");

        when(categoryService.saveCategory(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(saved);

        mvc.perform(post("/categories")
                .param("name", "Fiction"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));
    }

    @Test
    void shouldShowEditCategoryForm() throws Exception {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("Science");

        when(categoryService.getCategoryById(1L)).thenReturn(existing);

        mvc.perform(get("/categories/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/edit"))
            .andExpect(model().attributeExists("category"));
    }

    @Test
    void shouldUpdateCategoryAndRedirectToList() throws Exception {
        Category updated = new Category();
        updated.setId(1L);
        updated.setName("Updated Name");

        when(categoryService.saveCategory(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(updated);

        mvc.perform(post("/categories/1")
                .param("name", "Updated Name"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));
    }

    @Test
    void shouldDeleteCategoryAndRedirectToList() throws Exception {
        when(categoryService.hasBooks(1L)).thenReturn(false);
        
        mvc.perform(post("/categories/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));

        verify(categoryService).hasBooks(1L);
        verify(categoryService).deleteCategory(1L);
    }
    
    @Test
    void shouldNotDeleteCategoryWhenItHasBooksAndShowError() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        
        when(categoryService.hasBooks(1L)).thenReturn(true);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        
        mvc.perform(post("/categories/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"))
            .andExpect(flash().attributeExists("error"));

        verify(categoryService).hasBooks(1L);
        verify(categoryService).getCategoryById(1L);
        verify(categoryService, never()).deleteCategory(1L);
    }
}