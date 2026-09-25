package io.kestra.jdbc.repository;

import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.repositories.TenantRepositoryInterface;

public abstract class AbstractJdbcTenantRepository extends AbstractJdbcCrudRepository<Tenant> implements TenantRepositoryInterface {
    protected AbstractJdbcTenantRepository(io.kestra.jdbc.AbstractJdbcRepository<Tenant> jdbcRepository) {
        super(jdbcRepository);
    }

    @Override
    public Optional<Tenant> findById(String id) {
        return findOne(DSL.noCondition(), KEY_FIELD.eq(id));
    }

    @Override
    public List<Tenant> findAll() {
        return findAll(DSL.noCondition());
    }

    @Override
    protected Condition defaultFilter(String tenantId) {
        return DSL.noCondition();
    }

    @Override
    protected Condition defaultFilter() {
        return DSL.noCondition();
    }
}
