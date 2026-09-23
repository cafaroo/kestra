package io.kestra.repository.mysql;

import io.kestra.core.studio.InfrastructureDefinition;
import io.kestra.core.repositories.RepositoryBean;
import io.kestra.jdbc.repository.AbstractJdbcInfrastructureDefinitionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@RepositoryBean
@MysqlRepositoryEnabled
public class MysqlInfrastructureDefinitionRepository extends AbstractJdbcInfrastructureDefinitionRepository {
    @Inject
    public MysqlInfrastructureDefinitionRepository(@Named("studio_infrastructure") MysqlRepository<InfrastructureDefinition> repository) {
        super(repository);
    }
}
