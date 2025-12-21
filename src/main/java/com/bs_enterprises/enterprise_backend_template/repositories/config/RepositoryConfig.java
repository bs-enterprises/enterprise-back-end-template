package com.bs_enterprises.enterprise_backend_template.repositories.config;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import com.bs_enterprises.enterprise_backend_template.repositories.GenericMongoRepository;
import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.IndexingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean("keycloakUserRepository")
    public GenericMongoRepository<KeycloakUserModel> keycloakUserRepository(
            DatabaseService databaseService,
            IndexingService indexingService
    ) {
        return new GenericMongoRepository<>(
                KeycloakUserModel.class,
                MongoDBConstants.COLLECTION_USERS,
                databaseService,
                indexingService
        );
    }

}
