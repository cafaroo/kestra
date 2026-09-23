package io.kestra.repository.h2;

import io.kestra.core.studio.ReleaseTarget;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcReleaseTargetRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@H2RepositoryEnabled
public class H2ReleaseTargetRepository extends AbstractJdbcReleaseTargetRepository {
    @Inject
    public H2ReleaseTargetRepository(@Named("studio_release_targets") H2Repository<ReleaseTarget> repository) {
        super(repository);
    }
}
