package io.kestra.core.repositories;

import java.util.List;
import java.util.Optional;

import io.kestra.core.studio.ReleaseTarget;

public interface ReleaseTargetRepositoryInterface {
    Optional<ReleaseTarget> findById(String tenantId, String targetId);

    List<ReleaseTarget> findAll(String tenantId);

    ReleaseTarget save(ReleaseTarget target);
}
