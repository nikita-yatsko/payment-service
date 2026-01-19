package com.payment.service.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseMongoConfig {

    @Bean
    public CommandLineRunner runLiquibase() {
        return args -> {
            Database database = null;
            try {
                String url = "mongodb://localhost:27017/payment_db";
                database = DatabaseFactory.getInstance().openDatabase(url, null, null, null, null);
                if (!(database instanceof MongoLiquibaseDatabase)) {
                    throw new RuntimeException("Database is not a MongoDB instance");
                }
                Liquibase liquibase = new Liquibase("db.changelog/db.changelog-payment.yaml", new ClassLoaderResourceAccessor(), database);
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
