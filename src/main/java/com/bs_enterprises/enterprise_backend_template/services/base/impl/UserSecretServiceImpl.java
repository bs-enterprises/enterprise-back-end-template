package com.bs_enterprises.enterprise_backend_template.services.base.impl;

import com.bs_enterprises.enterprise_backend_template.annotations.CrudService;
import com.bs_enterprises.enterprise_backend_template.models.users.UserSecrets;
import com.bs_enterprises.enterprise_backend_template.repositories.AbstractMongoCrudService;
import com.bs_enterprises.enterprise_backend_template.repositories.GenericMongoRepository;
import com.bs_enterprises.enterprise_backend_template.services.base.UserSecretService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrudService
public class UserSecretServiceImpl
        extends AbstractMongoCrudService<UserSecrets>
        implements UserSecretService {

    public UserSecretServiceImpl(
            GenericMongoRepository<UserSecrets> repository
    ) {
        super(repository);
    }

    @Override
    protected List<String> allowedKeysForUpdate() {
        return new ArrayList<>();
    }

    @Override
    protected List<String> getFieldsRequiringInstantConversion() {
        return new ArrayList<>();
    }
}

