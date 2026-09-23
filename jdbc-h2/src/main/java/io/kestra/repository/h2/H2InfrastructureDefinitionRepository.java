package io.kestra.repository.h2;

import io.kestra.core.studio.InfrastructureDefinition;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcInfrastructureDefinitionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@H2RepositoryEnabled
public class H2InfrastructureDefinitionRepository extends AbstractJdbcInfrastructureDefinitionRepository {
    @Inject
    public H2InfrastructureDefinitionRepository(@Named("studio_infrastructure") H2Repository<InfrastructureDefinition> repository) {
        super(repository);
    }
}
