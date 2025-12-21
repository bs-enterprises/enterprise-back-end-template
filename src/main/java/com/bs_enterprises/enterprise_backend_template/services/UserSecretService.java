package com.bs_enterprises.enterprise_backend_template.services;

import com.bs_enterprises.enterprise_backend_template.models.users.UserSecrets;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public interface UserSecretService {

    UserSecrets save(UserSecrets userSecrets, String tenant, MongoTemplate optionalMongoTemplate);

    UserSecrets update(UserSecrets userSecrets, String tenant, MongoTemplate optionalMongoTemplate);

    UserSecrets findById(String id, String tenant, MongoTemplate optionalMongoTemplate);

    boolean deleteById(String id, String tenant, MongoTemplate optionalMongoTemplate);

}
