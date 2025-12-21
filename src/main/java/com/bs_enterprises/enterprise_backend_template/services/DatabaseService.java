package com.bs_enterprises.enterprise_backend_template.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing MongoDB database connections and administrative operations.
 */
@Service
public interface DatabaseService {


    /**
     * Switch to a new MongoDB database and return a new MongoTemplate instance.
     *
     * @param newDatabaseName name of the database to switch to
     * @return new MongoTemplate bound to the specified database
     */
    MongoTemplate changeDatabaseAndGetNewMongoTemplate(String newDatabaseName);

    /**
     * Drop a MongoDB database by its name (realm). This is irreversible.
     *
     * @param databaseName the name of the database to drop
     * @return true if the database existed and was dropped, false if it didn’t exist
     */
    boolean dropDatabaseByName(String databaseName);

    /**
     * List all databases available in the MongoDB instance.
     *
     * @return list of database names
     */
    List<String> listAllDatabases();

    /**
     * Check if a MongoDB database exists.
     *
     * @param databaseName the name of the database to check
     * @return true if the database exists, false otherwise
     */
    boolean databaseExists(String databaseName);
}
