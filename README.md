# Bookstore Management System

A Test-Driven Development (TDD) project demonstrating a Spring Boot bookstore management system with complete CRUD operations for books and categories.

## Build & Coverage
[![Build Status](https://github.com/AvanAvi/book-mgmt/actions/workflows/ci.yml/badge.svg)](https://github.com/AvanAvi/book-mgmt/actions)
[![Coverage Status](https://coveralls.io/repos/github/AvanAvi/book-mgmt/badge.svg?branch=main)](https://coveralls.io/github/AvanAvi/book-mgmt?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)

## Code Quality
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)

## Security & Reliability
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=AvanAvi_book-mgmt&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=AvanAvi_book-mgmt)

---

## About

Book management system built with Spring Boot 3.5.5 that handles books and categories through  REST API and Thymeleaf web interface. Implements bidirectional Book-Category relationships with full CRUD operations.

## Test Suite

Complete testing pyramid: Unit tests (JUnit 5 + Mockito), Integration tests (Testcontainers + MySQL), E2E tests (Selenium WebDriver + REST-assured). Achieves 100% code coverage (JaCoCo) and 100% mutation coverage (PITest).

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker
- Eclipse IDE

## Setup
```bash
git clone https://github.com/AvanAvi/book-mgmt.git
cd book-mgmt
```

Import in Eclipse: `File → Import → Existing Maven Projects`

---

## Running Tests

### Prerequisites: Clean Slate
```bash
# Kill all Java processes
pkill -9 java

# Stop and remove all Docker containers
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)

# Verify ports are free
lsof -i :9090
lsof -i :3308
```

Expected: Both commands return nothing (ports free) 

---

### TEST 1: Local Maven Commands

**A) Unit Tests**
```bash
./mvnw clean test
```
Expected: All unit tests pass 

**B) Integration Tests**
```bash
./mvnw clean verify
```
Expected: Unit + Integration tests pass 

**C) E2E Tests**
```bash
./mvnw clean verify -Pe2e-tests
```
Expected: E2E tests pass, MySQL container starts/stops automatically 

**STOP: Verify no containers running:**
```bash
docker ps
```
Expected: No containers 

---

### TEST 2: Eclipse - Unit and Integration Tests

**Run Unit Tests:**
1. In Eclipse, navigate to `src/test/java`
2. Right-click on the `com.attsw.bookstore` package
3. Select `Run As → JUnit Test`

Expected: All unit tests pass 

**Run Integration Tests:**
1. In Eclipse, navigate to `src/it/java`
2. Right-click on the `com.attsw.bookstore` package
3. Select `Run As → JUnit Test`

Expected: All integration tests pass (Testcontainers starts MySQL automatically) 

**Note:** Docker must be running for integration tests.

---

### TEST 3: Eclipse via Docker Compose

**Step 1: Start application**
```bash
docker-compose up --build
```
Wait for: "Started BookstoreManagementTddApplication" message

**Services started:**
- MySQL 5.7 (internal, no external port)
- Spring Boot app on http://localhost:9090

**Step 2: Run E2E tests in Eclipse**
1. Open Eclipse
2. Navigate to `src/e2e/java`
3. Right-click on `BookWebE2ETest.java`
4. Select `Run As → JUnit Test`

Expected: Tests pass 

**Step 3: Stop application**
```bash
# In terminal where docker-compose is running, press Ctrl+C
# Then run:
docker-compose down -v
```

**STOP: Verify cleanup:**
```bash
docker ps
lsof -i :9090
```
Expected: No containers, port 9090 free 

---

### TEST 4: Eclipse via Maven Docker Plugin

**Step 1: Check ports are free**
```bash
lsof -i :9090
lsof -i :3308
```
Expected: Both return nothing 

**Step 2: Start MySQL container**
```bash
./mvnw docker:start -Pdocker
```

Verify:
```bash
docker ps
```
Expected: MySQL container running on port 3308 

**Step 3: Start Spring Boot**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.devtools.restart.enabled=false
```
The local profile loads `application-local.properties` with MySQL container settings. DevTools auto-restart is disabled to prevent interruption during test execution.
Wait for: "Started BookstoreManagementTddApplication" message

**Step 4: Run E2E tests in Eclipse**
1. In Eclipse, navigate to `src/e2e/java`
2. Right-click on `CategoryWebE2ETest.java`
3. Select `Run As → JUnit Test`

Expected: Tests pass 

**Step 5: Stop Spring Boot**

In the terminal where Spring Boot is running, press `Ctrl+C`

**Step 6: Stop MySQL container**
```bash
./mvnw docker:stop -Pdocker
```

**STOP: Verify cleanup:**
```bash
docker ps
lsof -i :9090
lsof -i :3308
```
Expected: No containers, ports free 

---

## Quick Reference

**Technology Stack:** Java 17, Spring Boot 3.5.5, MySQL 5.7, Thymeleaf, Maven, JUnit 5, Mockito, Testcontainers, Selenium, REST-assured, JaCoCo, PITest

**Access Application:**
- Web UI: http://localhost:9090


**Test Structure:**
```
src/
├── test/java/    # Unit tests
├── it/java/      # Integration tests
└── e2e/java/     # E2E tests
```
