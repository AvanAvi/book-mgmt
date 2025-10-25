package com.attsw.bookstore.integration.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import jakarta.persistence.EntityManager;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.repository.BookRepository;
import com.attsw.bookstore.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CategoryWebControllerIT {

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
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void testGetAllCategories() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        categoryRepository.save(category);

        // When/Then
        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/list"))
            .andExpect(model().attributeExists("categories"))
            .andExpect(model().attribute("categories", 
                org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void testShowNewCategoryForm() throws Exception {
        mockMvc.perform(get("/categories/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/new"))
            .andExpect(model().attributeExists("category"));
    }

    @Test
    void testCreateCategory() throws Exception {
        mockMvc.perform(post("/categories")
                .param("name", "New Category"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));

        // Verify
        assertThat(categoryRepository.findAll()).hasSize(1);
        assertThat(categoryRepository.findAll().get(0).getName()).isEqualTo("New Category");
    }

    @Test
    void testShowEditCategoryForm() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Edit Test");
        category = categoryRepository.save(category);

        // When/Then
        mockMvc.perform(get("/categories/" + category.getId() + "/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/edit"))
            .andExpect(model().attributeExists("category"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Original Name");
        category = categoryRepository.save(category);

        // When/Then
        mockMvc.perform(post("/categories/" + category.getId())
                .param("name", "Updated Name"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));

        // Verify
        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteCategoryWithoutBooks() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Delete Me");
        category = categoryRepository.save(category);
        Long categoryId = category.getId();

        // When/Then
        mockMvc.perform(post("/categories/" + categoryId + "/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));

        // Verify
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void testDeleteCategoryWithBooks_shouldFail() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Has Books");
        category = categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Book in Category");
        book.setAuthor("Test Author");
        book.setCategory(category);
        bookRepository.save(book);
        
        // Clear persistence context to ensure fresh data fetch
        entityManager.flush();
        entityManager.clear();

        // When/Then
        mockMvc.perform(post("/categories/" + category.getId() + "/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"))
            .andExpect(flash().attributeExists("error"));

        // Verify category still exists
        assertThat(categoryRepository.findById(category.getId())).isPresent();
    }
}