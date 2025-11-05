package com.attsw.bookstore.integration.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.repository.BookRepository;
import com.attsw.bookstore.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class BookstoreWebControllerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testGetAllBooks() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Fiction");
        categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublishedDate(LocalDate.of(2024, 1, 15));
        book.setAvailable(true);
        book.setCategory(category);
        bookRepository.save(book);

        // When/Then
        mockMvc.perform(get("/books"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/list"))
            .andExpect(model().attributeExists("books"))
            .andExpect(model().attribute("books", 
                org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void testShowNewBookForm() throws Exception {
        mockMvc.perform(get("/books/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/new"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void testCreateBook() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Science");
        category = categoryRepository.save(category);

        // When/Then
        mockMvc.perform(post("/books")
                .param("title", "New Book")
                .param("author", "New Author")
                .param("isbn", "9876543210")
                .param("publishedDate", "2024-01-15")
                .param("available", "true")
                .param("category.id", category.getId().toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));

        // Verify
        assertThat(bookRepository.findAll()).hasSize(1);
        Book savedBook = bookRepository.findAll().get(0);
        assertThat(savedBook.getTitle()).isEqualTo("New Book");
        assertThat(savedBook.getAuthor()).isEqualTo("New Author");
        assertThat(savedBook.getIsbn()).isEqualTo("9876543210");
        assertThat(savedBook.getPublishedDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(savedBook.isAvailable()).isTrue();
    }

    @Test
    void testShowEditBookForm() throws Exception {
        // Given
        Book book = new Book();
        book.setTitle("Edit Test");
        book.setAuthor("Edit Author");
        book.setIsbn("1111111111");
        book.setPublishedDate(LocalDate.of(2024, 1, 1));
        book.setAvailable(true);
        book = bookRepository.save(book);

        // When/Then
        mockMvc.perform(get("/books/" + book.getId() + "/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("books/edit"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void testUpdateBook() throws Exception {
        // Given
        Book book = new Book();
        book.setTitle("Original Title");
        book.setAuthor("Original Author");
        book.setIsbn("0000000000");
        book.setPublishedDate(LocalDate.of(2023, 1, 1));
        book.setAvailable(false);
        book = bookRepository.save(book);

        // When/Then
        mockMvc.perform(post("/books/" + book.getId())
                .param("_method", "put")
                .param("title", "Updated Title")
                .param("author", "Updated Author")
                .param("isbn", "9999999999")
                .param("publishedDate", "2024-12-31")
                .param("available", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));

        // Verify
        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getAuthor()).isEqualTo("Updated Author");
        assertThat(updated.getIsbn()).isEqualTo("9999999999");
        assertThat(updated.getPublishedDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(updated.isAvailable()).isTrue();
    }

    @Test
    void testDeleteBook() throws Exception {
        // Given
        Book book = new Book();
        book.setTitle("Delete Me");
        book.setAuthor("Delete Author");
        book.setIsbn("0000000000");
        book.setPublishedDate(LocalDate.of(2024, 1, 1));
        book.setAvailable(true);
        book = bookRepository.save(book);
        Long bookId = book.getId();

        // When/Then
        mockMvc.perform(post("/books/" + bookId + "/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));

        // Verify
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }
}