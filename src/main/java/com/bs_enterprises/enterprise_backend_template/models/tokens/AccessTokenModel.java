package com.bs_enterprises.enterprise_backend_template.models.tokens;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = MongoDBConstants.COLLECTION_ACCESS_TOKENS) // Name of the MongoDB collection
@Data // Lombok annotation for generating getter/setter, toString, etc.
@NoArgsConstructor // Lombok annotation for no-argument constructor
@AllArgsConstructor
public class AccessTokenModel {

    @Id
    @Indexed(unique = true) // Index on the id field and enforce uniqueness
    @Field(MongoDBConstants.FIELD_ID)
    private String id;
    private String accessToken;
    private String verificationCode;

    // Lombok will automatically generate the constructor and getter/setter methods
}