# StradeX – Vehicle Rental Management System

A Java 17 + Spring Boot website for renting cars and bikes, built with Spring Data JPA, Thymeleaf and MySQL.

## Features
- Home page listing available vehicles with photos and daily prices (₹)
- Booking form with date checks, a live price preview and a server-side price calculation (days × daily rate)
- Double-booking protection: a vehicle can't be booked for dates that overlap another non-cancelled booking
- Admin page to add and delete vehicles
- Admin page to review bookings and mark them PENDING / CONFIRMED / CANCELLED
- Six sample vehicles (with photos) are added automatically the first time the database is empty

## Requirements
- JDK 17 or later
- MySQL Server (not needed for the quick demo below)
- Maven is optional – the included `mvnw` / `mvnw.cmd` wrapper downloads it for you
- IntelliJ IDEA, Eclipse or VS Code with Java extensions

## Quick demo (no MySQL)
Runs with an in-memory database. Data is lost when you stop the app.

```bash
# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
```

## Run with MySQL
1. Make sure MySQL is running on `localhost:3306`.
2. Give the app your MySQL password, either:
   - set an environment variable `MYSQL_PASSWORD` (and `MYSQL_USER` if it isn't `root`), or
   - open `src/main/resources/application.properties` and replace `YOUR_MYSQL_PASSWORD`.
3. Start it:
   ```bash
   ./mvnw spring-boot:run        # or: mvnw.cmd spring-boot:run   (or: mvn spring-boot:run)
   ```
4. Open http://localhost:8080 for the website and http://localhost:8080/admin for the admin page.

The database `stradex_db` is created automatically when the MySQL user has permission.

## Run the tests
```bash
./mvnw test
```

## Project structure
```
src/main/java/com/stradex/
  StradeXApplication.java        – starts the app
  model/Vehicle.java, Booking.java
  repository/                    – database access (Spring Data JPA)
  service/PricingService.java    – days × daily rate
  controller/WebsiteController.java – all pages and form actions
  config/SampleDataLoader.java   – adds sample vehicles on first run
src/main/resources/
  templates/ index.html, booking.html, admin.html
  static/css/style.css, static/images/
  application.properties, application-demo.properties
```

## Important limitations
This is an educational project. The admin page has **no login**, so don't put it on the public internet as-is.
Before a real launch, add admin authentication, keep the database password out of the code, and add payments
and email notifications if needed.
