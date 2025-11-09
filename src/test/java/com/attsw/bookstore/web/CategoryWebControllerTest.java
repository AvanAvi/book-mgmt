package com.attsw.bookstore.web;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.CategoryService;
import com.attsw.bookstore.service.BookService;

/**
 * Unit tests for {@link CategoryWebController}.
 */
@ExtendWith(MockitoExtension.class)
class CategoryWebControllerTest {

    @Mock
    private CategoryService categoryService;
    
    @Mock
    private BookService bookService;

    @Mock
    private Model model;

    @InjectMocks
    private CategoryWebController controller;

    @Test
    void shouldReturnListView() {
        Category category = new Category();
        category.setName("Fiction");
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category));
        when(bookService.getUncategorizedBooks()).thenReturn(Arrays.asList());

        String view = controller.list(model);

        assertEquals("categories/list", view);
        verify(model).addAttribute(eq("categories"), anyList());
        verify(model).addAttribute(eq("uncategorizedBooks"), anyList());
        verify(categoryService).getAllCategories();
        verify(bookService).getUncategorizedBooks();
    }

    @Test
    void shouldReturnNewCategoryView() {
        String view = controller.newCategory(model);

        assertEquals("categories/new", view);
        verify(model).addAttribute(eq("category"), any(Category.class));
    }

    @Test
    void shouldSaveCategoryAndRedirect() {
        Category category = new Category();

        String redirect = controller.saveCategory(category);

        assertEquals("redirect:/categories", redirect);
        verify(categoryService).saveCategory(category);
    }

    @Test
    void shouldReturnEditCategoryView() {
        Category category = new Category();
        category.setId(1L);
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        String view = controller.editCategory(1L, model);

        assertEquals("categories/edit", view);
        verify(model).addAttribute("category", category);
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void shouldUpdateCategoryAndRedirect() {
        Category category = new Category();
        category.setName("Updated Name");

        String redirect = controller.updateCategory(1L, category);

        assertEquals("redirect:/categories", redirect);
        assertEquals(1L, category.getId());
        verify(categoryService).saveCategory(category);
    }

    @Test
    void shouldDeleteCategoryWhenItHasNoBooks() {
        when(categoryService.hasBooks(1L)).thenReturn(false);
        
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String redirect = controller.deleteCategory(1L, redirectAttributes);

        assertEquals("redirect:/categories", redirect);
        verify(categoryService).hasBooks(1L);
        verify(categoryService).deleteCategory(1L);
        verifyNoInteractions(redirectAttributes);
    }
    
    @Test
    void shouldNotDeleteCategoryWhenItHasBooks() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        Book book = Book.withTitle("1984");
        book.setAuthor("George Orwell");
        book.setIsbn("9780451524935");
        book.setPublishedDate(LocalDate.of(1949, 6, 8));
        book.setAvailable(true);
        category.addBook(book);
        
        when(categoryService.hasBooks(1L)).thenReturn(true);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String redirect = controller.deleteCategory(1L, redirectAttributes);

        assertEquals("redirect:/categories", redirect);
        verify(categoryService).hasBooks(1L);
        verify(categoryService).getCategoryById(1L);
        verify(categoryService, never()).deleteCategory(1L);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }
}