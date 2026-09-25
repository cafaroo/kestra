package io.kestra.repository.postgres;

import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcTenantRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@PostgresRepositoryEnabled
public class PostgresTenantRepository extends AbstractJdbcTenantRepository {
    @Inject
    public PostgresTenantRepository(@Named("tenants") PostgresRepository<Tenant> repository) {
        super(repository);
    }
}
