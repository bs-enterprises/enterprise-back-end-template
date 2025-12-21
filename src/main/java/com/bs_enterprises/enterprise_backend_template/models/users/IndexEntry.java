package com.bs_enterprises.enterprise_backend_template.models.users;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexEntry {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    @Indexed(unique = true)
    private String id; // This will be the unique field value (e.g., email)

}
