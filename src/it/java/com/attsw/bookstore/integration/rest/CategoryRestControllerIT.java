package com.attsw.bookstore.integration.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.repository.BookRepository;
import com.attsw.bookstore.repository.CategoryRepository;

import io.restassured.RestAssured;

/**
 * TRUE Integration Test for CategoryRestController with real MySQL 5.7 database.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CategoryRestControllerIT {

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

	@LocalServerPort
	private int port;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BookRepository bookRepository;

	@BeforeEach
	void setup() {
		RestAssured.port = port;
		bookRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	void testGetAllCategories_whenEmpty() {
		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/categories")
		.then()
			.statusCode(200)
			.body("$", hasSize(0));
	}

	@Test
	void testGetAllCategories_withCategories() {
		Category cat1 = new Category();
		cat1.setName("Fiction");
		categoryRepository.save(cat1);

		Category cat2 = new Category();
		cat2.setName("Science");
		categoryRepository.save(cat2);

		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/categories")
		.then()
			.statusCode(200)
			.body("$", hasSize(2))
			.body("name", hasItems("Fiction", "Science"));
	}

	@Test
	void testGetCategoryById_found() {
		Category category = new Category();
		category.setName("Fiction");
		Category saved = categoryRepository.save(category);

		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/categories/" + saved.getId())
		.then()
			.statusCode(200)
			.body("id", equalTo(saved.getId().intValue()))
			.body("name", equalTo("Fiction"));
	}

	@Test
	void testGetCategoryById_notFound() {
		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/categories/999")
		.then()
			.statusCode(404);
	}

	@Test
	void testCreateCategory() {
		String newCategoryJson = "{\"name\":\"New Category\"}";

		Integer categoryId = given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(newCategoryJson)
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.body("name", equalTo("New Category"))
			.extract().path("id");

		Category dbCategory = categoryRepository.findById(categoryId.longValue()).orElse(null);
		assertThat(dbCategory).isNotNull();
		assertThat(dbCategory.getName()).isEqualTo("New Category");
	}

	@Test
	void testUpdateCategory() {
		Category category = new Category();
		category.setName("Old Name");
		category = categoryRepository.save(category);

		String updateJson = "{\"name\":\"Updated Name\"}";

		given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(updateJson)
		.when()
			.put("/api/categories/" + category.getId())
		.then()
			.statusCode(200)
			.body("name", equalTo("Updated Name"));

		Category dbCategory = categoryRepository.findById(category.getId()).orElse(null);
		assertThat(dbCategory).isNotNull();
		assertThat(dbCategory.getName()).isEqualTo("Updated Name");
	}

	@Test
	void testDeleteCategory_success() {
		Category category = new Category();
		category.setName("To Delete");
		category = categoryRepository.save(category);
		Long categoryId = category.getId();

		given()
		.when()
			.delete("/api/categories/" + categoryId)
		.then()
			.statusCode(204);

		assertThat(categoryRepository.findById(categoryId)).isEmpty();
	}

	@Test
	void testDeleteCategory_withBooks_shouldFail() {
		Category category = new Category();
		category.setName("Has Books");
		category = categoryRepository.save(category);

		Book book = new Book();
		book.setTitle("Book");
		book.setCategory(category);
		bookRepository.save(book);

		given()
		.when()
			.delete("/api/categories/" + category.getId())
		.then()
			.statusCode(400)
			.body("message", containsString("cannot be deleted"));

		assertThat(categoryRepository.findById(category.getId())).isPresent();
	}
}
