package io.kestra.repository.postgres;

import io.kestra.core.studio.ReleaseRecord;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseRecordRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@PostgresRepositoryEnabled
public class PostgresReleaseRecordRepository extends AbstractJdbcReleaseRecordRepository {
    @Inject
    public PostgresReleaseRecordRepository(@Named("studio_releases") PostgresRepository<ReleaseRecord> repository) {
        super(repository);
    }
}
