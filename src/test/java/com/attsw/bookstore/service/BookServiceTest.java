package com.attsw.bookstore.service;

import static org.junit.jupiter.api.Assertions.*;
import com.attsw.bookstore.model.Category; 
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookServiceImpl service;

    @Test
    void shouldGetAllBooks() {
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
        
        when(repository.findAll()).thenReturn(Arrays.asList(book1, book2));

        assertEquals(2, service.getAllBooks().size());
        verify(repository).findAll();
    }

    @Test
    void shouldGetBookById() {
        Book book = Book.withTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);
        
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        Book found = service.getBookById(1L);

        assertNotNull(found);
        verify(repository).findById(1L);
    }

    @Test
    void shouldReturnNullWhenBookNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertNull(service.getBookById(1L));
    }

    @Test
    void shouldSaveBook() {
        Book book = Book.withTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);
        
        Book saved = Book.withTitle("Clean Code");
        saved.setId(1L);
        saved.setAuthor("Robert Martin");
        saved.setIsbn("9780132350884");
        saved.setPublishedDate(LocalDate.of(2008, 8, 1));
        saved.setAvailable(true);
        
        when(repository.save(book)).thenReturn(saved);

        Book result = service.saveBook(book);

        assertNotNull(result.getId());
        verify(repository).save(book);
    }

    @Test
    void shouldUpdateBook() {
        Book existing = Book.withTitle("Old Title");
        existing.setId(1L);
        existing.setAuthor("Old Author");
        existing.setIsbn("Old ISBN");
        existing.setPublishedDate(LocalDate.of(2023, 1, 1));
        existing.setAvailable(false);
        
        Category oldCategory = new Category();
        oldCategory.setId(1L);
        oldCategory.setName("Old Category");
        existing.setCategory(oldCategory);
        
        Book updates = Book.withTitle("New Title");
        updates.setAuthor("New Author");
        updates.setIsbn("New ISBN");
        updates.setPublishedDate(LocalDate.of(2024, 12, 31));
        updates.setAvailable(true);
        
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("New Category");
        updates.setCategory(newCategory);
        
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Book result = service.updateBook(1L, updates);

        assertEquals("New Title", result.getTitle());
        assertEquals("New Author", result.getAuthor());
        assertEquals("New ISBN", result.getIsbn());
        assertEquals(LocalDate.of(2024, 12, 31), result.getPublishedDate());
        assertTrue(result.isAvailable());
        assertEquals(newCategory, result.getCategory());
        verify(repository).findById(1L);
        verify(repository).save(existing);
    }

    @Test
    void shouldDeleteBook() {
        service.deleteBook(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void updateBook_whenNotFound_returnsNull() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Book result = service.updateBook(99L, Book.withTitle(""));

        assertNull(result);
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }
    
    @Test
    void shouldGetUncategorizedBooks() {
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
        
        when(repository.findByCategoryIsNull()).thenReturn(Arrays.asList(book1, book2));

        List<Book> result = service.getUncategorizedBooks();

        assertEquals(2, result.size());
        verify(repository).findByCategoryIsNull();
    }
}