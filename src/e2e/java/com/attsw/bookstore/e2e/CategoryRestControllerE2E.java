package com.attsw.bookstore.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * End-to-End test for CategoryRestController.
 * Assumes Spring Boot application is ALREADY RUNNING.
 */
class CategoryRestControllerE2E { // NOSONAR

	private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));

	@BeforeEach
	void setup() {
		RestAssured.port = port;
		RestAssured.baseURI = "http://localhost";
	}

	@Test
	void testGetAllCategories() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/api/categories")
		.then()
			.statusCode(200)
			.body("$", isA(java.util.List.class));
	}

	@Test
	void testCreateAndRetrieveCategory() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"E2E Category Test\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.body("name", equalTo("E2E Category Test"))
			.extract().path("id");

		// Retrieve category
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/api/categories/" + categoryId)
		.then()
			.statusCode(200)
			.body("id", equalTo(categoryId))
			.body("name", equalTo("E2E Category Test"));
	}

	@Test
	void testUpdateCategory() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Original Category Name\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Update category
		given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Updated Category Name\"}")
		.when()
			.put("/api/categories/" + categoryId)
		.then()
			.statusCode(200)
			.body("name", equalTo("Updated Category Name"));
	}

	@Test
	void testDeleteCategory() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Category To Delete\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Delete category
		given()
		.when()
			.delete("/api/categories/" + categoryId)
		.then()
			.statusCode(204);

		// Verify deletion
		given()
		.when()
			.get("/api/categories/" + categoryId)
		.then()
			.statusCode(404);
	}

	@Test
	void testDeleteCategoryWithBooks_shouldFail() {
		// Create category
		Integer categoryId = given()
			.contentType(ContentType.JSON)
			.body("{\"name\":\"Category With Books\"}")
		.when()
			.post("/api/categories")
		.then()
			.statusCode(201)
			.extract().path("id");

		// Create book in this category
		String bookJson = String.format(
			"{\"title\":\"Book In Category\",\"category\":{\"id\":%d}}",
			categoryId
		);

		given()
			.contentType(ContentType.JSON)
			.body(bookJson)
		.when()
			.post("/api/books")
		.then()
			.statusCode(201);

		// Try to delete category - should fail
		given()
		.when()
			.delete("/api/categories/" + categoryId)
		.then()
			.statusCode(400)
			.body("message", containsString("cannot be deleted"));
	}
}
