package com.payment.service.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseMongoConfig {

    @Value("${spring.liquibase.url}")
    private String liquibaseUrl;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Bean
    public CommandLineRunner runLiquibase() {
        return args -> {
            Database database = null;
            try {
                database = DatabaseFactory.getInstance().openDatabase(liquibaseUrl, null, null, null, null);
                if (!(database instanceof MongoLiquibaseDatabase)) {
                    throw new RuntimeException("Database is not a MongoDB instance");
                }
                Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
                liquibase.update("");
                System.out.println("Liquibase migration applied successfully.");
            } catch (LiquibaseException e) {
                throw new RuntimeException("Failed to apply Liquibase migration", e);
            } finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (DatabaseException e) {
                        throw new RuntimeException("Failed to close database", e);
                    }
                }
            }
        };
    }
}
