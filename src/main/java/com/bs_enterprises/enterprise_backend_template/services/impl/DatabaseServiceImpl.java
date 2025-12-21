package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing MongoDB database connections and administrative operations.
 */
@Component
@AllArgsConstructor
@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    private final MongoClient mongoClient;

    /**
     * Switch to a new MongoDB database and return a new MongoTemplate instance.
     *
     * @param newDatabaseName name of the database to switch to
     * @return new MongoTemplate bound to the specified database
     */
    public MongoTemplate changeDatabaseAndGetNewMongoTemplate(String newDatabaseName) {
        return new MongoTemplate(this.mongoClient, newDatabaseName);
    }

    /**
     * Drop a MongoDB database by its name (realm). This is irreversible.
     *
     * @param databaseName the name of the database to drop
     * @return true if the database existed and was dropped, false if it didn’t exist
     */
    public boolean dropDatabaseByName(String databaseName) {
        try {
            List<String> existingDatabases = listAllDatabases();
            if (!existingDatabases.contains(databaseName)) {
                log.warn("⚠️ Database '{}' does not exist. Skipping drop operation.", databaseName);
                return false;
            }

            MongoDatabase db = mongoClient.getDatabase(databaseName);
            db.drop();
            log.info("✅ Successfully dropped MongoDB database: {}", databaseName);
            return true;
        } catch (Exception e) {
            log.error("❌ Failed to drop MongoDB database: {}", databaseName, e);
            throw new RuntimeException("Failed to drop database: " + databaseName, e);
        }
    }

    /**
     * List all databases available in the MongoDB instance.
     *
     * @return list of database names
     */
    public List<String> listAllDatabases() {
        List<String> databases = new ArrayList<>();
        mongoClient.listDatabaseNames().into(databases);
        log.info("📚 Available databases: {}", databases);
        return databases;
    }

    /**
     * Check if a MongoDB database exists.
     *
     * @param databaseName the name of the database to check
     * @return true if the database exists, false otherwise
     */
    public boolean databaseExists(String databaseName) {
        return listAllDatabases().contains(databaseName);
    }
}
