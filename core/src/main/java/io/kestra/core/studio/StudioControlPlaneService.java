package io.kestra.core.studio;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.kestra.core.exceptions.NotFoundException;
import io.kestra.core.repositories.InfrastructureDefinitionRepositoryInterface;
import io.kestra.core.repositories.ReleaseRecordRepositoryInterface;
import io.kestra.core.repositories.ReleaseTargetRepositoryInterface;
import io.kestra.core.tenant.TenantService;
import io.kestra.core.utils.IdUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StudioControlPlaneService {
    private final TenantService tenantService;
    private final ReleaseTargetRepositoryInterface targetRepository;
    private final InfrastructureDefinitionRepositoryInterface infrastructureRepository;
    private final ReleaseRecordRepositoryInterface releaseRepository;

    @Inject
    public StudioControlPlaneService(
        final TenantService tenantService,
        final ReleaseTargetRepositoryInterface targetRepository,
        final InfrastructureDefinitionRepositoryInterface infrastructureRepository,
        final ReleaseRecordRepositoryInterface releaseRepository) {
        this.tenantService = tenantService;
        this.targetRepository = targetRepository;
        this.infrastructureRepository = infrastructureRepository;
        this.releaseRepository = releaseRepository;
    }

    public List<ReleaseTarget> listTargets(final String tenantId) {
        tenantService.requireActiveTenant(tenantId);
        return targetRepository.findAll(tenantId);
    }

    public ReleaseTarget requireTarget(final String tenantId, final String targetId) {
        tenantService.requireActiveTenant(tenantId);
        return targetRepository.findById(tenantId, targetId)
            .orElseThrow(() -> new NotFoundException("Release target not found: '" + targetId + "'"));
    }

    public ReleaseTarget saveTarget(
        final String tenantId,
        final String targetId,
        final String name,
        final String runtimeUrl) {
        tenantService.requireActiveTenant(tenantId);

        Instant now = Instant.now();
        Optional<ReleaseTarget> previous = targetRepository.findById(tenantId, targetId);
        ReleaseTarget target = previous
            .map(existing -> existing.toBuilder()
                .name(name)
                .runtimeUrl(runtimeUrl)
                .updatedAt(now)
                .build())
            .orElseGet(() -> ReleaseTarget.builder()
                .tenantId(tenantId)
                .id(targetId)
                .name(name)
                .runtimeUrl(runtimeUrl)
                .health(RuntimeHealth.UNKNOWN)
                .status(ReleaseTargetStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());

        return targetRepository.save(target);
    }

    public Optional<InfrastructureDefinition> findInfrastructure(final String tenantId, final String targetId) {
        requireTarget(tenantId, targetId);
        return infrastructureRepository.findByTarget(tenantId, targetId);
    }

    public InfrastructureDefinition saveInfrastructure(
        final String tenantId,
        final String targetId,
        final List<InfrastructureDefinition.Boundary> boundaries,
        final List<InfrastructureDefinition.Node> nodes,
        final List<InfrastructureDefinition.Edge> edges,
        final String sourceCommit) {
        requireTarget(tenantId, targetId);

        InfrastructureDefinition previous = infrastructureRepository.findByTarget(tenantId, targetId).orElse(null);
        int revision = previous == null ? 1 : previous.getRevision() + 1;

        InfrastructureDefinition definition = InfrastructureDefinition.builder()
            .tenantId(tenantId)
            .targetId(targetId)
            .revision(revision)
            .boundaries(boundaries == null ? List.of() : List.copyOf(boundaries))
            .nodes(nodes == null ? List.of() : List.copyOf(nodes))
            .edges(edges == null ? List.of() : List.copyOf(edges))
            .sourceCommit(sourceCommit)
            .updatedAt(Instant.now())
            .build();

        return infrastructureRepository.save(definition);
    }

    public ReleaseRecord createRelease(
        final String tenantId,
        final String targetId,
        final String sourceCommit,
        final String actor) {
        ReleaseTarget target = requireTarget(tenantId, targetId);
        if (target.getStatus() != ReleaseTargetStatus.ACTIVE) {
            throw new IllegalStateException("Release target is disabled: '" + targetId + "'");
        }
        if (sourceCommit == null || sourceCommit.isBlank()) {
            throw new IllegalArgumentException("Source commit cannot be blank");
        }

        Instant now = Instant.now();
        ReleaseRecord release = ReleaseRecord.builder()
            .tenantId(tenantId)
            .id(IdUtils.create())
            .targetId(targetId)
            .sourceCommit(sourceCommit)
            .previousCommit(target.getDeployedCommit())
            .status(ReleaseStatus.READY)
            .actor(actor)
            .createdAt(now)
            .updatedAt(now)
            .build();

        targetRepository.save(
            target.toBuilder()
                .desiredCommit(sourceCommit)
                .updatedAt(now)
                .build()
        );

        return releaseRepository.save(release);
    }

    public List<ReleaseRecord> listReleases(final String tenantId, final String targetId) {
        requireTarget(tenantId, targetId);
        return releaseRepository.findByTarget(tenantId, targetId);
    }

    public ReleaseTarget recordObservedDeployment(
        final String tenantId,
        final String targetId,
        final String deployedCommit,
        final RuntimeHealth health) {
        ReleaseTarget target = requireTarget(tenantId, targetId);
        return targetRepository.save(
            target.toBuilder()
                .deployedCommit(deployedCommit)
                .health(health == null ? RuntimeHealth.UNKNOWN : health)
                .updatedAt(Instant.now())
                .build()
        );
    }
}
