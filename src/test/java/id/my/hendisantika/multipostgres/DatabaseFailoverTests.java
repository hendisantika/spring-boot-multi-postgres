package id.my.hendisantika.multipostgres;

import com.zaxxer.hikari.HikariDataSource;
import id.my.hendisantika.multipostgres.entity.Product;
import id.my.hendisantika.multipostgres.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simulates the PRIMARY database (DB1) being down and verifies the application fails over to the SECONDARY
 * database (DB2).
 */
@SpringBootTest
@Testcontainers
class DatabaseFailoverTests {

    @Container
    static PostgreSQLContainer secondary = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        // Nothing listens on port 1, so the primary connection attempt is refused immediately
        registry.add("spring.datasource.primary.jdbc-url", () -> "jdbc:postgresql://localhost:1/productdb");
        registry.add("spring.datasource.secondary.jdbc-url", secondary::getJdbcUrl);
        registry.add("spring.datasource.secondary.username", secondary::getUsername);
        registry.add("spring.datasource.secondary.password", secondary::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void failsOverToSecondaryDatabaseWhenPrimaryIsUnavailable() {
        assertThat(((HikariDataSource) dataSource).getJdbcUrl()).isEqualTo(secondary.getJdbcUrl());
    }

    @Test
    void persistsProductsInSecondaryDatabase() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1299.99"));
        product.setQuantity(10);

        Product saved = productRepository.save(product);

        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

}
