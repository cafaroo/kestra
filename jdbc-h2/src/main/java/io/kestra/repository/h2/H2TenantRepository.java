package io.kestra.repository.h2;

import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcTenantRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@H2RepositoryEnabled
public class H2TenantRepository extends AbstractJdbcTenantRepository {
    @Inject
    public H2TenantRepository(@Named("tenants") H2Repository<Tenant> repository) {
        super(repository);
    }
}
