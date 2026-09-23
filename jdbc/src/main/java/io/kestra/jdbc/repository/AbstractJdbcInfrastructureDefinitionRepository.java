package io.kestra.jdbc.repository;

import java.util.Optional;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.kestra.core.repositories.InfrastructureDefinitionRepositoryInterface;
import io.kestra.core.studio.InfrastructureDefinition;

public abstract class AbstractJdbcInfrastructureDefinitionRepository extends AbstractJdbcCrudRepository<InfrastructureDefinition>
    implements InfrastructureDefinitionRepositoryInterface {

    protected AbstractJdbcInfrastructureDefinitionRepository(io.kestra.jdbc.AbstractJdbcRepository<InfrastructureDefinition> repository) {
        super(repository);
    }

    @Override
    public Optional<InfrastructureDefinition> findByTarget(String tenantId, String targetId) {
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
