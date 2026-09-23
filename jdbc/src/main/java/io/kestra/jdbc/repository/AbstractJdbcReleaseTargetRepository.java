package io.kestra.jdbc.repository;

import java.util.Optional;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.kestra.core.repositories.ReleaseTargetRepositoryInterface;
import io.kestra.core.studio.ReleaseTarget;

public abstract class AbstractJdbcReleaseTargetRepository extends AbstractJdbcCrudRepository<ReleaseTarget>
    implements ReleaseTargetRepositoryInterface {

    protected AbstractJdbcReleaseTargetRepository(io.kestra.jdbc.AbstractJdbcRepository<ReleaseTarget> repository) {
        super(repository);
    }

    @Override
    public Optional<ReleaseTarget> findById(String tenantId, String targetId) {
        return findOne(tenantId, DSL.field("target_id", String.class).eq(targetId));
    }

    @Override
    protected Condition defaultFilter(String tenantId) {
        return buildTenantCondition(tenantId);
    }

    @Override
    protected Condition defaultFilter() {
        return DSL.noCondition();
    }
}
