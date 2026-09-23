package io.kestra.core.repositories;

import java.util.List;
import java.util.Optional;

import io.kestra.core.studio.ReleaseRecord;

public interface ReleaseRecordRepositoryInterface {
    Optional<ReleaseRecord> findById(String tenantId, String id);

    List<ReleaseRecord> findByTarget(String tenantId, String targetId);

    ReleaseRecord save(ReleaseRecord release);
}
