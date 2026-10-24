# Spring Boot Multi PostgreSQL with Automatic Failover

[![Java CI with Maven](https://github.com/hendisantika/spring-boot-multi-postgres/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/spring-boot-multi-postgres/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql)

A Spring Boot application demonstrating automatic database failover between multiple PostgreSQL 18 instances with a
RESTful CRUD API for product management.

## Features

- ✅ **Automatic Database Failover**: Seamlessly switches from primary (DB1) to secondary (DB2) database when primary is
  unavailable
- ✅ **PostgreSQL 18**: Latest PostgreSQL version running in Docker containers
- ✅ **RESTful CRUD API**: Complete product management endpoints
- ✅ **Docker Compose**: Easy multi-database setup and management
- ✅ **JPA/Hibernate**: ORM with automatic schema generation
- ✅ **Connection Pool**: HikariCP for efficient database connections
- ✅ **Lombok**: Reduces boilerplate code
- ✅ **Testcontainers**: Integration tests (including failover) run against real PostgreSQL 18 containers
- ✅ **CI & Dependabot**: GitHub Actions build on JDK 25 with grouped, automated dependency updates

## Technologies

- **Java 25** (LTS)
- **Spring Boot 4.1.1**
- **PostgreSQL 18**
- **Spring Data JPA**
- **HikariCP**
- **Lombok**
- **Testcontainers 2**
- **Docker & Docker Compose**
- **Maven 3.9 (via Maven Wrapper)**

## Database Configuration

### Primary Database (DB1)

- **Host**: `localhost:5435`
- **Database**: `productdb`
- **Username**: `yu71`
- **Password**: `53cret`

### Secondary Database (DB2) - Failover

- **Host**: `localhost:5434`
- **Database**: `productdb`
- **Username**: `yu71`
- **Password**: `53cret`

## Prerequisites

- Java 25 or higher
- Maven 3.9+ (or just use the bundled `./mvnw`)
- Docker & Docker Compose (also required to run the tests)
- PostgreSQL client (optional, for testing)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/hendisantika/spring-boot-multi-postgres.git
cd spring-boot-multi-postgres
```

### 2. Start PostgreSQL Containers

```bash
docker-compose up -d
```

Verify containers are running:

```bash
docker ps | grep postgres-db
```

### 3. Build the Application

```bash
./mvnw clean package -DskipTests
```

### 4. Run the Application

```bash
java -jar target/multi-postgres-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### Product Management

| Method   | Endpoint                           | Description             |
|----------|------------------------------------|-------------------------|
| `GET`    | `/api/products`                    | Get all products        |
| `GET`    | `/api/products/{id}`               | Get product by ID       |
| `POST`   | `/api/products`                    | Create a new product    |
| `PUT`    | `/api/products/{id}`               | Update product          |
| `DELETE` | `/api/products/{id}`               | Delete product          |
| `GET`    | `/api/products/search?name={name}` | Search products by name |

### Example Requests

#### Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High performance laptop",
    "price": 1299.99,
    "quantity": 10
  }'
```

#### Get All Products

```bash
curl http://localhost:8080/api/products
```

#### Get Product by ID

```bash
curl http://localhost:8080/api/products/1
```

#### Update Product

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High performance gaming laptop",
    "price": 1499.99,
    "quantity": 5
  }'
```

#### Delete Product

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

#### Search Products

```bash
curl http://localhost:8080/api/products/search?name=laptop
```

## Database Failover Demonstration

### Scenario 1: Both Databases Running

```bash
# Start both databases
docker-compose up -d

# Start application (connects to DB1)
java -jar target/multi-postgres-0.0.1-SNAPSHOT.jar
```

**Expected Log:**

```
Successfully connected to PRIMARY database (DB1) at jdbc:postgresql://localhost:5435/productdb
```

### Scenario 2: Primary Database Down

```bash
# Stop primary database
docker-compose stop postgres-db1

# Restart application
java -jar target/multi-postgres-0.0.1-SNAPSHOT.jar
```

**Expected Logs:**

```
Failed to connect to PRIMARY database (DB1): Connection to localhost:5435 refused
Attempting to connect to SECONDARY database (DB2)...
Successfully connected to SECONDARY database (DB2) at jdbc:postgresql://localhost:5434/productdb - Failover successful!
```

### Scenario 3: Restore Primary Database

```bash
# Start primary database again
docker-compose start postgres-db1

# Restart application (will connect back to DB1)
java -jar target/multi-postgres-0.0.1-SNAPSHOT.jar
```

## Project Structure

```
spring-boot-multi-postgres/
├── .github/
│   ├── dependabot.yml                               # Automated dependency updates
│   └── workflows/maven.yml                          # CI: build & test on JDK 25
├── src/
│   ├── test/
│   │   └── java/id/my/hendisantika/multipostgres/
│   │       ├── SpringBootMultiPostgresApplicationTests.java  # Boots app against primary DB
│   │       └── DatabaseFailoverTests.java                    # Verifies failover to DB2
│   └── main/
│       ├── java/
│       │   └── id/my/hendisantika/multipostgres/
│       │       ├── config/
│       │       │   └── DatabaseConfig.java          # Database failover configuration
│       │       ├── controller/
│       │       │   └── ProductController.java       # REST API endpoints
│       │       ├── entity/
│       │       │   └── Product.java                 # Product entity
│       │       ├── repository/
│       │       │   └── ProductRepository.java       # Data access layer
│       │       └── SpringBootMultiPostgresApplication.java
│       └── resources/
│           └── application.properties                # Database configuration
├── docker-compose.yml                                # Multi-database setup
├── pom.xml                                          # Maven dependencies
└── README.md
```

## How Failover Works

The `DatabaseConfig` class implements automatic failover logic:

1. **Connection Attempt to DB1**: The application first tries to connect to the primary database
2. **Connection Test**: Validates the connection by attempting to get a database connection
3. **Failover on Failure**: If DB1 is unavailable, automatically attempts to connect to DB2
4. **Success or Error**: Either connects to DB2 successfully or throws an exception if both are down

```java
// Simplified failover logic
try {
    // Try primary database
    HikariDataSource primaryDataSource = createDataSource(primaryConfig);
    testConnection(primaryDataSource);
    return primaryDataSource;
} catch (Exception e) {
    // Failover to secondary
    HikariDataSource secondaryDataSource = createDataSource(secondaryConfig);
    testConnection(secondaryDataSource);
    return secondaryDataSource;
}
```

## Docker Commands

### Start All Containers

```bash
docker-compose up -d
```

### Stop All Containers

```bash
docker-compose down
```

### Stop Specific Database

```bash
# Stop DB1
docker-compose stop postgres-db1

# Stop DB2
docker-compose stop postgres-db2
```

### View Logs

```bash
# View DB1 logs
docker logs postgres-db1

# View DB2 logs
docker logs postgres-db2
```

### Remove All (Including Volumes)

```bash
docker-compose down -v
```

## Troubleshooting

### Port Already in Use

If ports 5434 or 5435 are already in use:

1. Check what's using the port:
   ```bash
   lsof -i :5434
   lsof -i :5435
   ```

2. Modify `docker-compose.yml` to use different ports

### Connection Refused

Ensure Docker containers are running:

```bash
docker ps | grep postgres-db
```

### Application Won't Start

Check application logs for detailed error messages:

```bash
tail -f app.log
```

## Development

### Run in Development Mode

```bash
./mvnw spring-boot:run
```

### Run Tests

Tests use [Testcontainers](https://testcontainers.com/) to start throwaway `postgres:18` containers, so Docker must be
running. You don't need to start `docker-compose` first.

```bash
./mvnw verify
```

| Test class                                 | What it checks                                                          |
|--------------------------------------------|-------------------------------------------------------------------------|
| `SpringBootMultiPostgresApplicationTests`  | The application starts and connects to the PRIMARY database (DB1)      |
| `DatabaseFailoverTests`                    | With DB1 unreachable, the app fails over to DB2 and can persist products |

### Build Without Tests

```bash
./mvnw clean package -DskipTests
```

## Continuous Integration & Dependency Updates

- **GitHub Actions** (`.github/workflows/maven.yml`) runs `./mvnw verify` on JDK 25 (Temurin) for every push and pull
  request to `main`, including the Testcontainers integration tests. On `main` it also submits the Maven dependency
  graph to GitHub for better Dependabot alerts.
- **Dependabot** (`.github/dependabot.yml`) checks for updates to:
    - **Maven** (daily): Spring Boot, Testcontainers and Maven plugins are grouped, and other minor/patch bumps are
      bundled into one PR
    - **GitHub Actions** (daily): grouped into a single PR
    - **Docker Compose** (weekly): `postgres` images. Major versions are ignored because they need a manual data
      migration

## Author

- **Name**: hendisantika
- **Email**: hendisantika@yahoo.co.id
- **Link**: [s.id/hendisantika](https://s.id/hendisantika)
- **Telegram**: [@hendisantika34](https://t.me/hendisantika34)

## License

This project is open source and available under the [MIT License](LICENSE).

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
