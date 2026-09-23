package io.kestra.webserver.millblad.promote;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.kestra.core.utils.IdUtils;
import io.kestra.webserver.millblad.promote.PromotionModels.CandidateStatus;
import io.kestra.webserver.millblad.promote.PromotionModels.CompatibilityStatus;
import io.kestra.webserver.millblad.promote.PromotionModels.CreatePromotionCandidateRequest;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionCandidate;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PromotionCandidateService {
    private final PromotionTargetService targetService;
    private final PromotionCandidateStore candidateStore;

    @Inject
    public PromotionCandidateService(
        final PromotionTargetService targetService,
        final PromotionCandidateStore candidateStore
    ) {
        this.targetService = targetService;
        this.candidateStore = candidateStore;
    }

    public PromotionCandidate create(final String resolvedTenant, final CreatePromotionCandidateRequest request) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(request.artefactKind(), "artefactKind");
        requireText(request.artefactId(), "artefactId");
        requireText(request.sourceRevision(), "sourceRevision");
        requireText(request.targetId(), "targetId");

        PromotionTarget target = targetService.require(resolvedTenant, request.targetId());
        String id = IdUtils.create();

        PromotionCandidate candidate = new PromotionCandidate(
            id,
            request.artefactKind(),
            request.artefactId(),
            request.sourceRevision(),
            target.id(),
            CompatibilityStatus.UNKNOWN,
            CandidateStatus.CREATED,
            target.confirmationGate(),
            null,
            null,
            Instant.now()
        );

        candidateStore.put(resolvedTenant, candidate);
        return candidate;
    }

    public Optional<PromotionCandidate> find(final String resolvedTenant, final String candidateId) {
        return candidateStore.find(resolvedTenant, candidateId);
    }

    private static void requireText(final String value, final String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s must not be blank".formatted(name));
        }
    }
}
