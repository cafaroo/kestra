package io.kestra.core.repositories;

import java.util.Optional;

import io.kestra.core.studio.InfrastructureDefinition;

public interface InfrastructureDefinitionRepositoryInterface {
    Optional<InfrastructureDefinition> findByTarget(String tenantId, String targetId);

    InfrastructureDefinition save(InfrastructureDefinition definition);
}
