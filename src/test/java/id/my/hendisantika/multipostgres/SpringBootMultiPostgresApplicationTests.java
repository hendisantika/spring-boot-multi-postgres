package id.my.hendisantika.multipostgres;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the application against a real PostgreSQL 18 instance acting as the PRIMARY database (DB1).
 */
@SpringBootTest
@Testcontainers
class SpringBootMultiPostgresApplicationTests {

    @Container
    static PostgreSQLContainer primary = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.primary.jdbc-url", primary::getJdbcUrl);
        registry.add("spring.datasource.primary.username", primary::getUsername);
        registry.add("spring.datasource.primary.password", primary::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsUsingPrimaryDatabase() {
        assertThat(((HikariDataSource) dataSource).getJdbcUrl()).isEqualTo(primary.getJdbcUrl());
    }

}
