package rw.ac.auca.tapwallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "rw.ac.auca.tapwallet.repository")
public class PersistenceConfig {
}
