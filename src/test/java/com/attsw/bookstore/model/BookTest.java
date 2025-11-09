package com.attsw.bookstore.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Book}.
 */
class BookTest {

    @Test
    void bookShouldHaveTitleAuthorIsbnPublishedDateAndAvailability() {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("9780132350884", book.getIsbn());
        assertEquals(LocalDate.of(2008, 8, 1), book.getPublishedDate());
        assertTrue(book.isAvailable());
    }
    
    @Test
    void bookShouldHaveACategory() {
        Category category = new Category();
        category.setName("Software Engineering");

        Book book = new Book();
        book.setCategory(category);

        assertEquals(category, book.getCategory());
    }
    
    @Test
    void whenAddedToCategory_bookKnowsItsCategory() {
        Category category = new Category();
        category.setName("Software Engineering");

        Book book = new Book();
        book.setTitle("Refactoring");

        category.addBook(book);         

        assertEquals(category, book.getCategory());  
    }
}