package io.kestra.repository.postgres;

import io.kestra.core.studio.ReleaseTarget;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseTargetRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@PostgresRepositoryEnabled
public class PostgresReleaseTargetRepository extends AbstractJdbcReleaseTargetRepository {
    @Inject
    public PostgresReleaseTargetRepository(@Named("studio_release_targets") PostgresRepository<ReleaseTarget> repository) {
        super(repository);
    }
}
