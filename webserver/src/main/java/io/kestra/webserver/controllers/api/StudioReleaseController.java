package io.kestra.webserver.controllers.api;

import java.util.List;

import io.kestra.core.studio.ReleaseRecord;
import io.kestra.core.studio.ReleaseTarget;
import io.kestra.core.studio.StudioControlPlaneService;
import io.kestra.core.tenant.TenantService;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Controller("/api/v1/{tenant}/studio")
@Requires(property = "millblad.studio.enabled", value = "true")
public class StudioReleaseController {
    private final TenantService tenantService;
    private final StudioControlPlaneService controlPlaneService;

    @Inject
    public StudioReleaseController(
        final TenantService tenantService,
        final StudioControlPlaneService controlPlaneService) {
        this.tenantService = tenantService;
        this.controlPlaneService = controlPlaneService;
    }

    @Get("/release-targets")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Releases" }, summary = "List release targets")
    public List<ReleaseTarget> listTargets() {
        return controlPlaneService.listTargets(tenantService.resolveTenant());
    }

    @Put("/release-targets/{targetId}")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Releases" }, summary = "Create or update a release target")
    public ReleaseTarget saveTarget(
        @PathVariable String targetId,
        @Valid @Body SaveTargetRequest request) {
        return controlPlaneService.saveTarget(
            tenantService.resolveTenant(),
            targetId,
            request.name(),
            request.runtimeUrl()
        );
    }

    @Get("/release-targets/{targetId}/releases")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Releases" }, summary = "List release history for a target")
    public List<ReleaseRecord> listReleases(@PathVariable String targetId) {
        return controlPlaneService.listReleases(tenantService.resolveTenant(), targetId);
    }

    @Post("/release-targets/{targetId}/releases")
    @ExecuteOn(TaskExecutors.IO)
    @Operation(tags = { "Studio Releases" }, summary = "Prepare an immutable release for a target")
    public HttpResponse<ReleaseRecord> createRelease(
        @PathVariable String targetId,
        @Valid @Body CreateReleaseRequest request,
        HttpRequest<?> httpRequest) {
        String actor = httpRequest.getHeaders().get("X-Kestra-User-Id");
        ReleaseRecord release = controlPlaneService.createRelease(
            tenantService.resolveTenant(),
            targetId,
            request.sourceCommit(),
            actor
        );
        return HttpResponse.created(release);
    }

    public record SaveTargetRequest(
        @NotBlank String name,
        String runtimeUrl) {
    }

    public record CreateReleaseRequest(
        @NotBlank String sourceCommit) {
    }
}
