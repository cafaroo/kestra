package io.kestra.repository.mysql;

import io.kestra.core.studio.ReleaseTarget;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseTargetRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@MysqlRepositoryEnabled
public class MysqlReleaseTargetRepository extends AbstractJdbcReleaseTargetRepository {
    @Inject
    public MysqlReleaseTargetRepository(@Named("studio_release_targets") MysqlRepository<ReleaseTarget> repository) {
        super(repository);
    }
}
