package com.attsw.bookstore.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@DataJpaTest
@Testcontainers
class BookRepositoryIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    BookRepository repository;
    
    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindBook() {
        Book book = Book.withTitle("Clean Code");

        Book saved = repository.save(book);

        assertNotNull(saved.getId());
        assertEquals("Clean Code", saved.getTitle());
    }

    @Test
    void shouldFindAllBooks() {
        Book book1 = Book.withTitle("Clean Code");
        Book book2 = Book.withTitle("Refactoring");

        repository.save(book1);
        repository.save(book2);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldFindBookById() {
        Book book = Book.withTitle("Clean Code");
        Book saved = repository.save(book);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertEquals("Clean Code", repository.findById(saved.getId()).get().getTitle());
    }

    @Test
    void shouldDeleteBook() {
        Book book = Book.withTitle("Clean Code");
        Book saved = repository.save(book);

        repository.deleteById(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldFindBooksWithNullCategory() {
        // Create and save a category
        Category fiction = new Category();
        fiction.setName("Fiction");
        Category savedCategory = categoryRepository.save(fiction);
        
        // Create books - one with category, two without
        Book bookWithCategory = Book.withTitle("1984");
        bookWithCategory.setCategory(savedCategory);
        
        Book uncategorized1 = Book.withTitle("Clean Code");
        Book uncategorized2 = Book.withTitle("Refactoring");
        
        repository.save(bookWithCategory);
        repository.save(uncategorized1);
        repository.save(uncategorized2);
        
        // Find uncategorized books
        List<Book> uncategorizedBooks = repository.findByCategoryIsNull();
        
        assertEquals(2, uncategorizedBooks.size());
        assertTrue(uncategorizedBooks.stream()
            .anyMatch(b -> "Clean Code".equals(b.getTitle())));
        assertTrue(uncategorizedBooks.stream()
            .anyMatch(b -> "Refactoring".equals(b.getTitle())));
    }
}