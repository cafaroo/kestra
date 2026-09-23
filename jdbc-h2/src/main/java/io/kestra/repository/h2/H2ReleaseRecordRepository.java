package io.kestra.repository.h2;

import io.kestra.core.studio.ReleaseRecord;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseRecordRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@H2RepositoryEnabled
public class H2ReleaseRecordRepository extends AbstractJdbcReleaseRecordRepository {
    @Inject
    public H2ReleaseRecordRepository(@Named("studio_releases") H2Repository<ReleaseRecord> repository) {
        super(repository);
    }
}
