package io.kestra.core.tenant;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.kestra.core.contexts.configuration.StudioConfiguration;
import io.kestra.core.exceptions.AlreadyExistsException;
import io.kestra.core.exceptions.ForbiddenException;
import io.kestra.core.exceptions.NotFoundException;
import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.models.tenants.TenantStatus;
import io.kestra.core.repositories.TenantRepositoryInterface;
import io.kestra.core.validations.validator.TenantIdValidator;

import io.micronaut.context.BeanProvider;
import io.micronaut.http.BasicHttpAttributes;
import io.micronaut.http.context.ServerRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TenantService {
    public static final String MAIN_TENANT = "main";
    public static final String TENANT_PATH_ATTRIBUTE = "tenant";

    private final StudioConfiguration studioConfiguration;
    private final BeanProvider<TenantRepositoryInterface> tenantRepositoryProvider;

    @Inject
    public TenantService(
        final StudioConfiguration studioConfiguration,
        final BeanProvider<TenantRepositoryInterface> tenantRepositoryProvider) {
        this.studioConfiguration = studioConfiguration;
        this.tenantRepositoryProvider = tenantRepositoryProvider;
    }

    /**
     * Resolves the request tenant. Ordinary OSS keeps the historical single-tenant {@code main}
     * behavior; Central Studio resolves the {@code tenant} route variable instead.
     */
    public String resolveTenant() {
        if (!studioConfiguration.enabled()) {
            return MAIN_TENANT;
        }

        return ServerRequestContext.currentRequest()
            .flatMap(BasicHttpAttributes::getRouteMatchInfo)
            .map(route -> route.getVariableValues().get(TENANT_PATH_ATTRIBUTE))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .orElse(MAIN_TENANT);
    }

    /**
     * Lists active tenant identifiers. Ordinary OSS remains single-tenant.
     */
    public List<String> listTenants() {
        if (!studioConfiguration.enabled()) {
            return List.of(MAIN_TENANT);
        }

        return listTenantRecords().stream()
            .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
            .map(Tenant::getId)
            .toList();
    }

    public List<Tenant> listTenantRecords() {
        requireStudio();
        return repository().findAll().stream()
            .sorted(Comparator.comparing(Tenant::getId))
            .toList();
    }

    public Optional<Tenant> findTenant(final String tenantId) {
        if (!studioConfiguration.enabled()) {
            return MAIN_TENANT.equals(tenantId)
                ? Optional.of(
                    Tenant.builder()
                        .id(MAIN_TENANT)
                        .name("Main")
                        .status(TenantStatus.ACTIVE)
                        .createdAt(Instant.EPOCH)
                        .updatedAt(Instant.EPOCH)
                        .build()
                )
                : Optional.empty();
        }
        return repository().findById(tenantId);
    }

    public Tenant requireActiveTenant(final String tenantId) {
        Tenant tenant = findTenant(tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found: '" + tenantId + "'"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new ForbiddenException("Tenant is disabled: '" + tenantId + "'");
        }
        return tenant;
    }

    public Tenant createTenant(final String tenantId, final String name) {
        requireStudio();
        if (!TenantIdValidator.isValid(tenantId)) {
            throw new IllegalArgumentException("Invalid tenant id: '" + tenantId + "'");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be blank");
        }
        if (repository().findById(tenantId).isPresent()) {
            throw AlreadyExistsException.of("Tenant", tenantId);
        }

        Instant now = Instant.now();
        return repository().save(
            Tenant.builder()
                .id(tenantId)
                .name(name.trim())
                .status(TenantStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build()
        );
    }

    public Tenant disableTenant(final String tenantId) {
        requireStudio();
        Tenant current = repository().findById(tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found: '" + tenantId + "'"));

        if (current.getStatus() == TenantStatus.DISABLED) {
            return current;
        }

        return repository().save(
            current.toBuilder()
                .status(TenantStatus.DISABLED)
                .updatedAt(Instant.now())
                .build()
        );
    }

    public boolean isStudioEnabled() {
        return studioConfiguration.enabled();
    }

    public boolean isDefinitionOnly() {
        return studioConfiguration.definitionOnly();
    }

    private TenantRepositoryInterface repository() {
        if (!tenantRepositoryProvider.isPresent()) {
            throw new IllegalStateException("Tenant repository is unavailable");
        }
        return tenantRepositoryProvider.get();
    }

    private void requireStudio() {
        if (!studioConfiguration.enabled()) {
            throw new IllegalStateException("Tenant registry is only available when Millblad Studio is enabled");
        }
    }
}
