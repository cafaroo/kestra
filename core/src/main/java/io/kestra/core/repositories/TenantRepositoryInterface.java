package io.kestra.core.repositories;

import java.util.List;
import java.util.Optional;

import io.kestra.core.models.tenants.Tenant;

public interface TenantRepositoryInterface {
    Optional<Tenant> findById(String id);

    List<Tenant> findAll();

    Tenant save(Tenant tenant);
}
