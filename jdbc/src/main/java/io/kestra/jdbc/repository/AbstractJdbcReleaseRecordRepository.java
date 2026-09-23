package io.kestra.jdbc.repository;

import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import io.kestra.core.repositories.ReleaseRecordRepositoryInterface;
import io.kestra.core.studio.ReleaseRecord;

public abstract class AbstractJdbcReleaseRecordRepository extends AbstractJdbcCrudRepository<ReleaseRecord>
    implements ReleaseRecordRepositoryInterface {

    protected AbstractJdbcReleaseRecordRepository(io.kestra.jdbc.AbstractJdbcRepository<ReleaseRecord> repository) {
        super(repository);
    }

    @Override
    public Optional<ReleaseRecord> findById(String tenantId, String id) {
        return findOne(tenantId, DSL.field("id", String.class).eq(id));
    }

    @Override
    public List<ReleaseRecord> findByTarget(String tenantId, String targetId) {
        return find(tenantId, DSL.field("target_id", String.class).eq(targetId), DSL.field("created_at").desc());
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
