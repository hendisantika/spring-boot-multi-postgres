package id.my.hendisantika.multipostgres.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-postgres
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 16/10/25
 * Time: 12.05
 * To change this template use File | Settings | File Templates.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "id.my.hendisantika.multipostgres.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.primary.jdbc-url}")
    private String primaryJdbcUrl;

    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryPassword;

    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDriverClassName;

    @Value("${spring.datasource.secondary.jdbc-url}")
    private String secondaryJdbcUrl;

    @Value("${spring.datasource.secondary.username}")
    private String secondaryUsername;

    @Value("${spring.datasource.secondary.password}")
    private String secondaryPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String secondaryDriverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Try to connect to primary database
        try {
            HikariDataSource primaryDataSource = new HikariDataSource();
            primaryDataSource.setJdbcUrl(primaryJdbcUrl);
            primaryDataSource.setUsername(primaryUsername);
            primaryDataSource.setPassword(primaryPassword);
            primaryDataSource.setDriverClassName(primaryDriverClassName);
            primaryDataSource.setConnectionTimeout(3000); // 3 seconds timeout

            // Test the connection
            try (Connection connection = primaryDataSource.getConnection()) {
                log.info("Successfully connected to PRIMARY database (DB1) at {}", primaryJdbcUrl);
                return primaryDataSource;
            }
        } catch (Exception e) {
            log.error("Failed to connect to PRIMARY database (DB1): {}", e.getMessage());
            log.info("Attempting to connect to SECONDARY database (DB2)...");

            try {
                HikariDataSource secondaryDataSource = new HikariDataSource();
                secondaryDataSource.setJdbcUrl(secondaryJdbcUrl);
                secondaryDataSource.setUsername(secondaryUsername);
                secondaryDataSource.setPassword(secondaryPassword);
                secondaryDataSource.setDriverClassName(secondaryDriverClassName);
                secondaryDataSource.setConnectionTimeout(3000);

                // Test the connection
                try (Connection connection = secondaryDataSource.getConnection()) {
                    log.info("Successfully connected to SECONDARY database (DB2) at {} - Failover successful!", secondaryJdbcUrl);
                    return secondaryDataSource;
                }
            } catch (Exception ex) {
                log.error("Failed to connect to SECONDARY database (DB2): {}", ex.getMessage());
                throw new RuntimeException("Both primary and secondary databases are unavailable", ex);
            }
        }
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("id.my.hendisantika.multipostgres.entity")
                .persistenceUnit("primary")
                .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
