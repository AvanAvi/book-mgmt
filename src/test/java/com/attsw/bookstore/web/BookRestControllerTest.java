package com.attsw.bookstore.web;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.BookService;

/**
 * Unit tests for {@link BookRestController}.
 */
@ExtendWith(MockitoExtension.class)
class BookRestControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookRestController controller;

    @Test
    void shouldReturnAllBooks() {
        Book book1 = Book.withTitle("Clean Code");
        book1.setAuthor("Robert Martin");
        book1.setIsbn("9780132350884");
        book1.setPublishedDate(LocalDate.of(2008, 8, 1));
        book1.setAvailable(true);
        
        Book book2 = Book.withTitle("Refactoring");
        book2.setAuthor("Martin Fowler");
        book2.setIsbn("9780201485677");
        book2.setPublishedDate(LocalDate.of(1999, 7, 8));
        book2.setAvailable(true);
        
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        List<Book> result = controller.all();

        assertEquals(2, result.size());
        verify(bookService).getAllBooks();
    }

    @Test
    void shouldCreateBook() {
        Book input = Book.withTitle("TDD");
        input.setAuthor("Kent Beck");
        input.setIsbn("0321146530");
        input.setPublishedDate(LocalDate.of(2002, 11, 18));
        input.setAvailable(true);
        
        Book saved = Book.withTitle("TDD");
        saved.setId(1L);
        saved.setAuthor("Kent Beck");
        saved.setIsbn("0321146530");
        saved.setPublishedDate(LocalDate.of(2002, 11, 18));
        saved.setAvailable(true);
        
        when(bookService.saveBook(input)).thenReturn(saved);

        Book result = controller.create(input);

        assertNotNull(result.getId());
        assertEquals("TDD", result.getTitle());
        verify(bookService).saveBook(input);
    }

    @Test
    void shouldReturnBookById() {
        Book book = Book.withTitle("Clean Code");
        book.setId(1L);
        book.setAuthor("Robert Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);
        
        when(bookService.getBookById(1L)).thenReturn(book);

        Book result = controller.one(1L);

        assertEquals(1L, result.getId());
        assertEquals("Clean Code", result.getTitle());
        verify(bookService).getBookById(1L);
    }

    @Test
    void shouldUpdateBook() {
        Book updated = Book.withTitle("Updated Title");
        updated.setId(1L);
        updated.setAuthor("Updated Author");
        updated.setIsbn("9999999999");
        updated.setPublishedDate(LocalDate.of(2024, 12, 31));
        updated.setAvailable(true);
        
        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(updated);

        Book result = controller.update(1L, new Book());

        assertEquals("Updated Title", result.getTitle());
        verify(bookService).updateBook(eq(1L), any(Book.class));
    }

    @Test
    void shouldDeleteBook() {
        doNothing().when(bookService).deleteBook(1L);

        controller.delete(1L);

        verify(bookService).deleteBook(1L);
    }
    
    @Test
    void shouldReturn404WhenBookNotFound() {
        when(bookService.getBookById(999L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> controller.one(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("not found"));
        verify(bookService).getBookById(999L);
    }
}