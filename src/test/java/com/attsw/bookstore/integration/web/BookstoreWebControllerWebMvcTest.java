package com.attsw.bookstore.integration.web;

import com.attsw.bookstore.web.BookstoreWebController;
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

        when(bookService.saveBook(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);

        mvc.perform(post("/books")
                .param("title", "TDD")
                .param("author", "Kent")
                .param("isbn", "123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));
    }

    @Test
    void shouldShowEditBookForm() throws Exception {
        Book existing = Book.withTitle("Clean Code");
        existing.setId(1L);

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

        when(bookService.updateBook(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(updated);

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("author", "Updated Author")
                .param("isbn", "new456"))
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