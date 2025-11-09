package com.attsw.bookstore.web;

import java.time.LocalDate;
import com.attsw.bookstore.service.CategoryService;
import com.attsw.bookstore.service.BookService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.attsw.bookstore.model.Book;

/**
 * MVC slice tests for book web views.
 */
@WebMvcTest(BookstoreWebController.class)
class BookstoreWebControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;
    
    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldShowBookListPage() throws Exception {
        Book b = Book.withTitle("Clean Code");
        b.setAuthor("Robert Martin");
        b.setIsbn("9780132350884");
        b.setPublishedDate(LocalDate.of(2008, 8, 1));
        b.setAvailable(true);
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(b));

        mvc.perform(get("/books"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/list"))
            .andExpect(model().attributeExists("books"));
    }

    @Test
    void shouldShowAddBookForm() throws Exception {
        mvc.perform(get("/books/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/new"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void shouldSaveBookAndRedirectToList() throws Exception {
        Book saved = Book.withTitle("TDD");
        saved.setId(1L);
        saved.setAuthor("Kent Beck");
        saved.setIsbn("0321146530");
        saved.setPublishedDate(LocalDate.of(2002, 11, 18));
        saved.setAvailable(true);
        when(bookService.saveBook(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);

        mvc.perform(post("/books")
                .param("title", "TDD")
                .param("author", "Kent")
                .param("isbn", "0321146530")
                .param("publishedDate", "2002-11-18")
                .param("available", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));
    }

    @Test
    void shouldShowEditBookForm() throws Exception {
        Book existing = Book.withTitle("Clean Code");
        existing.setId(1L);
        existing.setAuthor("Robert Martin");
        existing.setIsbn("9780132350884");
        existing.setPublishedDate(LocalDate.of(2008, 8, 1));
        existing.setAvailable(true);

        when(bookService.getBookById(1L)).thenReturn(existing);

        mvc.perform(get("/books/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/edit"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void shouldUpdateBookAndRedirectToList() throws Exception {
        Book updated = Book.withTitle("Updated Title");
        updated.setId(1L);
        updated.setAuthor("Updated Author");
        updated.setIsbn("9999999999");
        updated.setPublishedDate(LocalDate.of(2024, 12, 31));
        updated.setAvailable(true);

        when(bookService.updateBook(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(updated);

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("author", "Updated Author")
                .param("isbn", "9999999999")
                .param("publishedDate", "2024-12-31")
                .param("available", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));
    }

    @Test
    void shouldDeleteBookAndRedirectToList() throws Exception {
        mvc.perform(post("/books/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteBook(1L);
    }
}