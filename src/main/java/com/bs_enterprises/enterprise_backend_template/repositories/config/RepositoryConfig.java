package com.bs_enterprises.enterprise_backend_template.repositories.config;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import com.bs_enterprises.enterprise_backend_template.models.users.UserSecrets;
import com.bs_enterprises.enterprise_backend_template.repositories.GenericMongoRepository;
import com.bs_enterprises.enterprise_backend_template.services.base.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.base.IndexingService;
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

    @Bean("userSecretsRepository")
    public GenericMongoRepository<UserSecrets> userSecretsRepository(
            DatabaseService databaseService,
            IndexingService indexingService
    ) {
        return new GenericMongoRepository<>(
                UserSecrets.class,
                MongoDBConstants.COLLECTION_USER_SECRETS,
                databaseService,
                indexingService
        );
    }

}
