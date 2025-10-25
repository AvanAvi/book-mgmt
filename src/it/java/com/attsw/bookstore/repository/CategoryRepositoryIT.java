package com.attsw.bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.attsw.bookstore.model.Category;

/**
 * Integration test for CategoryRepository using @DataJpaTest with Testcontainers MySQL 5.7.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryIT {

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7")
			.withDatabaseName("test_bookstore")
			.withUsername("test")
			.withPassword("test");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void testJpaMapping() {
		Category category = new Category();
		category.setName("Test Category");
		Category saved = entityManager.persistFlushFind(category);

		assertThat(saved.getName()).isEqualTo("Test Category");
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getId()).isPositive();
	}

	@Test
	void testFindAll() {
		Category category1 = new Category();
		category1.setName("Fiction");
		category1 = entityManager.persistFlushFind(category1);

		Category category2 = new Category();
		category2.setName("Science");
		category2 = entityManager.persistFlushFind(category2);

		Iterable<Category> categories = categoryRepository.findAll();

		assertThat(categories).containsExactly(category1, category2);
	}

	@Test
	void testFindById() {
		Category category = new Category();
		category.setName("Fiction");
		Category saved = entityManager.persistFlushFind(category);

		Category found = categoryRepository.findById(saved.getId()).orElse(null);

		assertThat(found).isNotNull();
		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getName()).isEqualTo("Fiction");
	}

	@Test
	void testSave() {
		Category category = new Category();
		category.setName("New Category");
		Category saved = categoryRepository.save(category);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getId()).isPositive();

		Category found = entityManager.find(Category.class, saved.getId());
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("New Category");
	}

	@Test
	void testUpdate() {
		Category category = new Category();
		category.setName("Old Name");
		category = entityManager.persistFlushFind(category);

		category.setName("Updated Name");
		categoryRepository.saveAndFlush(category);   
		entityManager.refresh(category);             

		assertThat(category.getName()).isEqualTo("Updated Name"); 
}

	@Test
	void testDelete() {
		Category category = new Category();
		category.setName("To Delete");
		category = entityManager.persistFlushFind(category);
		Long id = category.getId();

		categoryRepository.delete(category);
		entityManager.flush();

		Category found = entityManager.find(Category.class, id);
		assertThat(found).isNull();
	}
}
