package com.attsw.bookstore.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * End-to-End test for Book Web UI.
 * Assumes Spring Boot application is ALREADY RUNNING.
 * Uses Selenium WebDriver to test the web interface.
 */
class BookWebE2ETest {

    private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));
    private static String baseUrl = "http://localhost:" + port;

    private WebDriver driver;
    private WebDriverWait wait;

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testHomePageTitle() {
        driver.get(baseUrl);
        assertThat(driver.getTitle()).isEqualTo("Book Management");
    }

    @Test
    void testNavigateToNewBookPage() {
        driver.get(baseUrl);
        driver.findElement(By.linkText("Books")).click();
        driver.findElement(By.linkText("+ New Book")).click();
        assertThat(driver.getCurrentUrl()).contains("/books/new");
    }

    @Test
    void testCreateNewBook() throws Exception {
        driver.get(baseUrl + "/books/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        driver.findElement(By.id("title")).sendKeys("E2E Test Book");
        driver.findElement(By.id("author")).sendKeys("E2E Author");
        driver.findElement(By.id("isbn")).sendKeys("1234567890");
        
        WebElement createDateInput = driver.findElement(By.id("publishedDate"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = '2024-11-04';", createDateInput);
        
        driver.findElement(By.id("available")).click();
        driver.findElement(By.id("submitButton")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/books"),
                ExpectedConditions.urlToBe(baseUrl + "/")
        ));

        driver.get(baseUrl + "/books");
        assertThat(driver.getPageSource()).contains("E2E Test Book");
    }
    
    @Test
    void testViewAllBooks() {
        driver.get(baseUrl + "/books");

        // Verify books table exists
        assertThat(driver.findElements(By.id("booksTable"))).isNotEmpty();
    }

    @Test
    void testEditBook() {
        driver.get(baseUrl + "/books");

        // Find first edit button (if exists)
        var editButtons = driver.findElements(By.cssSelector("a[href*='/books/'][href*='/edit']"));

        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();

            // Wait for edit page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("title")));

            // Verify we're on edit page
            assertThat(driver.getCurrentUrl()).contains("/books/");
            assertThat(driver.getCurrentUrl()).contains("/edit");

            // Change multiple fields
            var titleInput = driver.findElement(By.id("title"));
            titleInput.clear();
            titleInput.sendKeys("Updated Title");

            var authorInput = driver.findElement(By.id("author"));
            authorInput.clear();
            authorInput.sendKeys("Updated Author");

            var isbnInput = driver.findElement(By.id("isbn"));
            isbnInput.clear();
            isbnInput.sendKeys("9999999999");

            WebElement editDateInput = driver.findElement(By.id("publishedDate"));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '2024-12-31';", editDateInput);

            // Toggle available checkbox if needed
            var availableCheckbox = driver.findElement(By.id("available"));
            if (!availableCheckbox.isSelected()) {
                availableCheckbox.click();
            }

            // Submit
            driver.findElement(By.id("submitButton")).click();

            // Wait for redirect
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/books"),
                    ExpectedConditions.urlToBe(baseUrl + "/")
            ));

            // Navigate to books page to verify
            driver.get(baseUrl + "/books");

            // Verify update
            assertThat(driver.getPageSource()).contains("Updated Title");
        }
    }

    @Test
    void testDeleteBook() {
        driver.get(baseUrl + "/books");

        // Count books before deletion
        var deleteButtonsBefore = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
        int countBefore = deleteButtonsBefore.size();

        if (countBefore > 0) {
            // Click first delete button
            deleteButtonsBefore.get(0).click();

            // Wait for page refresh
            wait.until(ExpectedConditions.stalenessOf(deleteButtonsBefore.get(0)));

            // Count books after deletion
            var deleteButtonsAfter = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
            int countAfter = deleteButtonsAfter.size();

            // Verify one less book
            assertThat(countAfter).isLessThan(countBefore);
        }
    }
}