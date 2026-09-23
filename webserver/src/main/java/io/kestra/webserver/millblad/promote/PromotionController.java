package io.kestra.webserver.millblad.promote;

import java.util.List;

import io.kestra.core.exceptions.NotFoundException;
import io.kestra.core.tenant.TenantService;
import io.kestra.webserver.millblad.promote.PromotionModels.CreatePromotionCandidateRequest;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionCandidate;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;

@Controller("/api/v1/{tenant}/promote")
public class PromotionController {
    private final TenantService tenantService;
    private final PromotionTargetService targetService;
    private final PromotionCandidateService candidateService;

    @Inject
    public PromotionController(
        final TenantService tenantService,
        final PromotionTargetService targetService,
        final PromotionCandidateService candidateService
    ) {
        this.tenantService = tenantService;
        this.targetService = targetService;
        this.candidateService = candidateService;
    }

    @Get("/targets")
    @Operation(tags = {"Promote"}, summary = "List Millblad promotion targets")
    public List<PromotionTarget> listTargets() {
        return targetService.list(tenantService.resolveTenant());
    }

    @Post("/candidates")
    @Operation(tags = {"Promote"}, summary = "Create a Millblad promotion candidate")
    public PromotionCandidate createCandidate(@Body final CreatePromotionCandidateRequest request) {
        return candidateService.create(tenantService.resolveTenant(), request);
    }

    @Get("/candidates/{candidateId}")
    @Operation(tags = {"Promote"}, summary = "Read a Millblad promotion candidate")
    public PromotionCandidate getCandidate(@PathVariable final String candidateId) {
        String resolvedTenant = tenantService.resolveTenant();
        return candidateService.find(resolvedTenant, candidateId)
            .orElseThrow(() -> new NotFoundException("Promotion candidate not found: '%s'".formatted(candidateId)));
    }
}