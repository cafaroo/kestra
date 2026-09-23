package io.kestra.repository.mysql;

import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcTenantRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@MysqlRepositoryEnabled
public class MysqlTenantRepository extends AbstractJdbcTenantRepository {
    @Inject
    public MysqlTenantRepository(@Named("tenants") MysqlRepository<Tenant> repository) {
        super(repository);
    }
}
