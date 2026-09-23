package io.kestra.webserver.controllers.api;

import java.util.List;

import io.kestra.core.exceptions.NotFoundException;
import io.kestra.core.studio.InfrastructureDefinition;
import io.kestra.core.studio.StudioControlPlaneService;
import io.kestra.core.tenant.TenantService;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;

@Controller("/api/v1/{tenant}/studio/infrastructure")
@Requires(property = "millblad.studio.enabled", value = "true")
public class StudioInfrastructureController {
    private final TenantService tenantService;
    private final StudioControlPlaneService controlPlaneService;

    @Inject
    public StudioInfrastructureController(
        final TenantService tenantService,
        final StudioControlPlaneService controlPlaneService) {
        this.tenantService = tenantService;
        this.controlPlaneService = controlPlaneService;
    }

    @Get("/{targetId}")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Infrastructure" }, summary = "Read infrastructure desired state for a release target")
    public InfrastructureDefinition get(@PathVariable String targetId) {
        String tenant = tenantService.resolveTenant();
        return controlPlaneService.findInfrastructure(tenant, targetId)
            .orElseThrow(() -> new NotFoundException("Infrastructure definition not found for target '" + targetId + "'"));
    }

    @Put("/{targetId}")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Infrastructure" }, summary = "Save infrastructure desired state for a release target")
    public InfrastructureDefinition save(
        @PathVariable String targetId,
        @Body SaveInfrastructureRequest request) {
        return controlPlaneService.saveInfrastructure(
            tenantService.resolveTenant(),
            targetId,
            request.boundaries(),
            request.nodes(),
            request.edges(),
            request.sourceCommit()
        );
    }

    public record SaveInfrastructureRequest(
        List<InfrastructureDefinition.Boundary> boundaries,
        List<InfrastructureDefinition.Node> nodes,
        List<InfrastructureDefinition.Edge> edges,
        String sourceCommit) {
    }
}
