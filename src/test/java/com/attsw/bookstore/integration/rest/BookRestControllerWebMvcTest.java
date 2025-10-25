package com.attsw.bookstore.integration.rest;  
import com.attsw.bookstore.web.BookRestController;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.BookService;

@WebMvcTest(BookRestController.class)
class BookRestControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void shouldReturnJsonListOfBooks() throws Exception {
        Book b = Book.withTitle("Clean Code");
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(b));

        mvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void shouldCreateBookViaPost() throws Exception {
        Book saved = Book.withTitle("Refactoring");
        saved.setId(1L);
        saved.setAuthor("Martin Fowler");
        saved.setIsbn("0201485672");

        when(bookService.saveBook(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Refactoring",
                      "author": "Martin Fowler",
                      "isbn": "0201485672"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Refactoring"));
    }

    @Test
    void shouldReturnSingleBookById() throws Exception {
        Book saved = Book.withTitle("Clean Code");
        saved.setId(1L);

        when(bookService.getBookById(1L)).thenReturn(saved);

        mvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void shouldUpdateExistingBookViaPut() throws Exception {
        Book updated = Book.withTitle("New Title");
        updated.setId(1L);
        updated.setAuthor("New Author");
        updated.setIsbn("1111111111");

        when(bookService.updateBook(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(updated);

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "New Title",
                      "author": "New Author",
                      "isbn": "1111111111"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void shouldDeleteBookViaDelete() throws Exception {
        mvc.perform(delete("/api/books/1"))
            .andExpect(status().isNoContent());
    }
}