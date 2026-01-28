package com.bs_enterprises.enterprise_backend_template.models.users;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = MongoDBConstants.COLLECTION_USER_SECRETS)
// Defaults to collection name based on class name or config (not explicitly
// "user_secrets")
public class UserSecrets {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    @Indexed
    private String id; // user Id
    private String keycloakUserId;

}