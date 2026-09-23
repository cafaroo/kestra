package io.kestra.repository.mysql;

import io.kestra.core.studio.ReleaseRecord;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseRecordRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@MysqlRepositoryEnabled
public class MysqlReleaseRecordRepository extends AbstractJdbcReleaseRecordRepository {
    @Inject
    public MysqlReleaseRecordRepository(@Named("studio_releases") MysqlRepository<ReleaseRecord> repository) {
        super(repository);
    }
}
