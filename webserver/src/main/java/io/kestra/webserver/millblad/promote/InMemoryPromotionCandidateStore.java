package io.kestra.webserver.millblad.promote;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.kestra.webserver.millblad.promote.PromotionModels.PromotionCandidate;

import jakarta.inject.Singleton;

/**
 * Bootstrap store for the first Promote slice.
 *
 * Promotion history/release persistence will replace this with a durable implementation.
 */
@Singleton
public class InMemoryPromotionCandidateStore implements PromotionCandidateStore {
    private final Map<String, Map<String, PromotionCandidate>> candidatesByTenant = new ConcurrentHashMap<>();

    @Override
    public void put(final String resolvedTenant, final PromotionCandidate candidate) {
        candidatesByTenant
            .computeIfAbsent(resolvedTenant, ignored -> new ConcurrentHashMap<>())
            .put(candidate.id(), candidate);
    }

    @Override
    public Optional<PromotionCandidate> find(final String resolvedTenant, final String candidateId) {
        return Optional.ofNullable(
            candidatesByTenant
                .getOrDefault(resolvedTenant, Map.of())
                .get(candidateId)
        );
    }
}
