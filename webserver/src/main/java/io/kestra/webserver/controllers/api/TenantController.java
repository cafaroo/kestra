package io.kestra.webserver.controllers.api;

import java.util.List;

import io.kestra.core.models.tenants.Tenant;
import io.kestra.core.tenant.TenantService;
import io.kestra.core.validations.TenantId;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Controller("/api/v1/tenants")
@Requires(property = "millblad.studio.enabled", value = "true")
public class TenantController {
    private final TenantService tenantService;

    @Inject
    public TenantController(final TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Get
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Tenants" }, summary = "List Central Studio tenants")
    public List<Tenant> list() {
        return tenantService.listTenantRecords();
    }

    @Get("/{id}")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Tenants" }, summary = "Get a Central Studio tenant")
    public Tenant get(@PathVariable String id) {
        return tenantService.findTenant(id)
            .orElseThrow(() -> new io.kestra.core.exceptions.NotFoundException("Tenant not found: '" + id + "'"));
    }

    @Post
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Tenants" }, summary = "Create a Central Studio tenant")
    public HttpResponse<Tenant> create(@Valid @Body CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenant(request.id(), request.name());
        return HttpResponse.created(tenant);
    }

    @Post("/{id}/disable")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Tenants" }, summary = "Disable a Central Studio tenant")
    public Tenant disable(@PathVariable String id) {
        return tenantService.disableTenant(id);
    }

    public record CreateTenantRequest(
        @TenantId String id,
        @NotBlank String name) {
    }
}
