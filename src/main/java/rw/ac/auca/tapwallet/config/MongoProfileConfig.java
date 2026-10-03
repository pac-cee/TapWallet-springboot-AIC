package rw.ac.auca.tapwallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile("mongodb")
@EnableMongoRepositories(basePackages = "rw.ac.auca.tapwallet.document")
public class MongoProfileConfig {
}
