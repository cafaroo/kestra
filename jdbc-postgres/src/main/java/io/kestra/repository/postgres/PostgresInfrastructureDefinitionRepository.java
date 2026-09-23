package io.kestra.repository.postgres;

import io.kestra.core.studio.InfrastructureDefinition;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcInfrastructureDefinitionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@PostgresRepositoryEnabled
public class PostgresInfrastructureDefinitionRepository extends AbstractJdbcInfrastructureDefinitionRepository {
    @Inject
    public PostgresInfrastructureDefinitionRepository(@Named("studio_infrastructure") PostgresRepository<InfrastructureDefinition> repository) {
        super(repository);
    }
}
