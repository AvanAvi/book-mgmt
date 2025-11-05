package com.attsw.bookstore.web;  
import java.time.LocalDate;

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
        b.setAuthor("Robert Martin");
        b.setIsbn("9780132350884");
        b.setPublishedDate(LocalDate.of(2008, 8, 1));
        b.setAvailable(true);
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
        saved.setPublishedDate(LocalDate.of(1999, 7, 8));
        saved.setAvailable(true);

        when(bookService.saveBook(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Refactoring",
                      "author": "Martin Fowler",
                      "isbn": "0201485672",
                      "publishedDate": "1999-07-08",
                	  "available": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Refactoring"));
    }

    @Test
    void shouldReturnSingleBookById() throws Exception {
        Book saved = Book.withTitle("Clean Code");
        saved.setId(1L);
        saved.setAuthor("Robert Martin");
        saved.setIsbn("9780132350884");
        saved.setPublishedDate(LocalDate.of(2008, 8, 1));
        saved.setAvailable(true);

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
        updated.setPublishedDate(LocalDate.of(2024, 12, 31));
        updated.setAvailable(true);
        when(bookService.updateBook(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(updated);

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "New Title",
                      "author": "New Author",
                      "isbn": "1111111111",
                      "publishedDate": "2024-12-31",
                	  "available": true
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