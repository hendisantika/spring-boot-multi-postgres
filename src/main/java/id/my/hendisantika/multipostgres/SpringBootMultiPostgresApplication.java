package id.my.hendisantika.multipostgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SpringBootMultiPostgresApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringBootMultiPostgresApplication.class, args);
    }

}
