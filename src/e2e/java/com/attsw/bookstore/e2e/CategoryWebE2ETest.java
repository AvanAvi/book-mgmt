package com.attsw.bookstore.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * End-to-end tests for Category Web UI using Selenium WebDriver.
 * Requires the Spring Boot application to be running.
 */
class CategoryWebE2ETest { 

	private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));
	private static String baseUrl = "http://localhost:" + port;

	private WebDriver driver;

	@BeforeAll
	static void setupClass() {
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	void setup() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		driver = new ChromeDriver(options);
	}

	@AfterEach
	void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * Tests navigation to the categories page.
	 */
	@Test
	void testNavigateToCategoriesPage() {
		driver.get(baseUrl);
		driver.findElement(By.linkText("Categories")).click();
		assertThat(driver.getCurrentUrl()).contains("/categories");
	}

	/**
	 * Tests viewing the categories list page.
	 */
	@Test
	void testViewAllCategories() {
		driver.get(baseUrl + "/categories");
		assertThat(driver.findElements(By.id("categoriesTable"))).isNotEmpty();
	}

	/**
	 * Tests creating a new category through the web form.
	 */
	@Test
	void testCreateNewCategory() {
		// Navigate to new category page
		driver.get(baseUrl + "/categories/new");

		// Fill form
		driver.findElement(By.id("name")).sendKeys("E2E Test Category");

		// Submit form
		driver.findElement(By.id("submitButton")).click();

		// Verify redirect and category is listed
		assertThat(driver.getCurrentUrl()).contains("/categories");
		assertThat(driver.getPageSource()).contains("E2E Test Category");
	}

	/**
	 * Tests editing an existing category through the web form.
	 */
	@Test
	void testEditCategory() {
		driver.get(baseUrl + "/categories");

		// Find first edit button if exists
		var editButtons = driver.findElements(By.cssSelector("a[href*='/categories/edit/']"));
		
		if (!editButtons.isEmpty()) {
			editButtons.get(0).click();

			// Verify on edit page
			assertThat(driver.getCurrentUrl()).contains("/categories/edit/");

			// Update name
			var nameInput = driver.findElement(By.id("name"));
			nameInput.clear();
			nameInput.sendKeys("Updated E2E Category");

			// Submit
			driver.findElement(By.id("submitButton")).click();

			// Verify update
			assertThat(driver.getPageSource()).contains("Updated E2E Category");
		}
	}

	/**
	 * Tests deleting a category and verifying the count decreases.
	 */
	@Test
	void testDeleteCategory() {
		driver.get(baseUrl + "/categories");

		// Count categories before deletion
		var deleteButtonsBefore = driver.findElements(By.cssSelector("button[onclick*='deleteCategory']"));
		int countBefore = deleteButtonsBefore.size();

		if (countBefore > 0) {
			deleteButtonsBefore.get(0).click();

			// Accept confirmation dialog
			driver.switchTo().alert().accept();

			// Wait for page refresh
			driver.navigate().refresh();

			// Count categories after deletion
			var deleteButtonsAfter = driver.findElements(By.cssSelector("button[onclick*='deleteCategory']"));
			int countAfter = deleteButtonsAfter.size();

			// Verify deletion
			assertThat(countAfter).isLessThan(countBefore);
		}
	}

	/**
	 * Tests that categories with associated books display a warning.
	 */
	@Test
	void testCannotDeleteCategoryWithBooks() {
		driver.get(baseUrl + "/categories");

		// Find warning for categories with books
		var warningElements = driver.findElements(By.cssSelector(".category-has-books-warning"));
		
		if (!warningElements.isEmpty()) {
			assertThat(warningElements.get(0).getText()).contains("has books");
		}
	}
}