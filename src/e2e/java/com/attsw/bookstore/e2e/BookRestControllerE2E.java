package com.attsw.bookstore.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * End-to-End test for BookRestController.
 * Assumes Spring Boot application is ALREADY RUNNING on the specified port.
 * No Spring annotations - this is a plain JUnit test.
 */
class BookRestControllerE2E { // NOSONAR

	private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));

	@BeforeEach
	void setup() {
		RestAssured.port = port;
		RestAssured.baseURI = "http://localhost";
	}

	@Test
	void testGetAllBooks() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/api/books")
		.then()
			.statusCode(200)
			.body("$", isA(java.util.List.class));
	}

	@Test
	void testCreateAndRetrieveBook() {
		// First create a category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"E2E Test Category\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Create a book with that category
		String bookJson = String.format(
			"{\"title\":\"E2E Test Book\",\"category\":{\"id\":%d}}",
			categoryId
		);

		Integer bookId = given()
			.contentType(ContentType.JSON)
			.body(bookJson)
		.when()
			.post("/api/books")
		.then()
			.statusCode(201)
			.body("title", equalTo("E2E Test Book"))
			.body("category.id", equalTo(categoryId))
			.extract().path("id");

		// Verify book can be retrieved
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/api/books/" + bookId)
		.then()
			.statusCode(200)
			.body("id", equalTo(bookId))
			.body("title", equalTo("E2E Test Book"));
	}

	@Test
	void testUpdateBook() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Update Test Category\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Create book
		String bookJson = String.format(
			"{\"title\":\"Original Title\",\"category\":{\"id\":%d}}",
			categoryId
		);

		Integer bookId = given()
			.contentType(ContentType.JSON)
			.body(bookJson)
		.when()
			.post("/api/books")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Update book
		String updateJson = String.format(
			"{\"title\":\"Updated Title\",\"category\":{\"id\":%d}}",
			categoryId
		);

		given()
			.contentType(ContentType.JSON)
			.body(updateJson)
		.when()
			.put("/api/books/" + bookId)
		.then()
			.statusCode(200)
			.body("title", equalTo("Updated Title"));
	}

	@Test
	void testDeleteBook() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Delete Test Category\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Create book
		String bookJson = String.format(
			"{\"title\":\"Book To Delete\",\"category\":{\"id\":%d}}",
			categoryId
		);

		Integer bookId = given()
			.contentType(ContentType.JSON)
			.body(bookJson)
		.when()
			.post("/api/books")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Delete book
		given()
		.when()
			.delete("/api/books/" + bookId)
		.then()
			.statusCode(204);

		// Verify deletion
		given()
		.when()
			.get("/api/books/" + bookId)
		.then()
			.statusCode(404);
	}
}